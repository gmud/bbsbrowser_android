package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ContentListActivity extends GestureListActivity {

	private List<DocObject> list;

	private DocObject doc;
	private DocObject selectedDoc;
	private boolean titleMode;
	private String tag;
	private boolean renderColorContent = true;
	private boolean notitleReImage = true;
	private boolean qmdImage = false;
	private int actionParam;
	private boolean needClear = true;
	
	private String [] words;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());

		this.getListView().setDividerHeight(0);
		this.getListView().setBackgroundResource(R.color.app_background_doc);

		doc = (DocObject) this.getIntent().getSerializableExtra("doc");
		boolean topTenTitle = this.getIntent().getBooleanExtra("title", false);
		actionParam = this.getIntent().getIntExtra("action", ActionFactory.DEFAULT_PARAM);
		titleMode = topTenTitle?true:UserUtil.TITLE_MODE;
		
		list = new ArrayList<DocObject>();
		setListAdapter(new ContentListAdapter(list));

		if (doc != null) {
			setThisDoc(doc);			
			scrollAction.addActionWhenScroll(getListView());			
			scrollAction.refresh();
		}
		
		words = UserUtil.getPostShortWords(this);
		renderColorContent = SettingActivity.renderColorContent(this);
		notitleReImage = SettingActivity.notitleReimage(this);
		qmdImage = SettingActivity.qmdImage(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 4, (UserUtil.HTML_MODE ? "普通模式" : "看图模式")).setIcon(android.R.drawable.ic_menu_gallery);
		menu.add(0, 2, 3, ("刷新帖子")).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 3, 1, ("上一主题")).setIcon(android.R.drawable.ic_media_rew);
		menu.add(0, 4, 2, ("下一主题")).setIcon(android.R.drawable.ic_media_ff);
		return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(1).setTitle((UserUtil.HTML_MODE ? "普通模式" : "看图模式"));
		if (titleMode) {
			menu.findItem(3).setVisible(true);
			menu.findItem(4).setVisible(true);
		} else {
			menu.findItem(3).setVisible(false);
			menu.findItem(4).setVisible(false);
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			switchHtmlMode();
			break;
		case 2:
			scrollAction.refresh();
			break;
		case 3:
			fetchContentFromTag("a=b");
			break;
		case 4:
			fetchContentFromTag("a=a");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("html".equals(actionName)) {
			switchHtmlMode();
		} else if ("back".equals(actionName)) {
			finish();
		} else if ("refresh".equals(actionName)) {
			scrollAction.refresh();
		}
	}

	private static final int CONTEXT_MENU_DRW = 0;
	private static final int CONTEXT_MENU_CCC = 1;
	private static final int CONTEXT_MENU_FWD = 2;
	private static final int CONTEXT_MENU_EDT = 3;

	
	@Override
    protected Dialog onCreateDialog(int id) {
		final EditText txt = new EditText(this);
		switch (id) {
		case CONTEXT_MENU_CCC:
			if (this.selectedDoc == null) return null;
			return new AlertDialog.Builder(this).setTitle("输入版面")
				.setView(txt)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						cccDocContent(selectedDoc, txt.getText().toString());
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
			}).create();
		case CONTEXT_MENU_FWD:
			if (this.selectedDoc == null) return null;
			return new AlertDialog.Builder(this).setTitle("输入用户")
				.setView(txt)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						fwdDocContent(selectedDoc, txt.getText().toString());
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
			}).create();
		default:
			return null;
		}
	}
		
	private static final int REQ_CONTENT_DETAIL  = 0;
	private static final int REQ_CONTENT_DRAW  = 1;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CONTENT_DETAIL) {
			if (resultCode == Activity.RESULT_OK) {
				if (ContentDetailActivity.POST_ACTION_EDIT.equals(data.getAction())) {
					DocObject n = (DocObject)data.getSerializableExtra("doc");
					this.selectedDoc.setContent(n.getContent());
				} else if (ContentDetailActivity.POST_ACTION_DEL.equals(data.getAction())) {
					scrollAction.remove(selectedDoc);
					if (list.size() == 0) {
						setResult(RESULT_OK, new Intent().setAction(ContentDetailActivity.POST_ACTION_DEL));
						finish();
					}
				}
				((BaseAdapter) ContentListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
			} else if (resultCode == Activity.RESULT_CANCELED) {
			}
		} else if (requestCode == REQ_CONTENT_DRAW) {
			if (resultCode == Activity.RESULT_OK) {
				reDrawContentResult(this.selectedDoc);
			}
		}
	}
	
	private void setThisDoc(DocObject doc) {
		this.doc = doc;
		if (this.doc != null)
			this.setTitle("[" + doc.getBoard().getName() + "]:" + doc.getTitle());
	}
	
	private void titleClick(int position) {
		if (UserUtil.CURRENT_USER_ID == null || actionParam != ActionFactory.DEFAULT_PARAM) return;
		DocObject doc = list.get(position);
		editOrReDocContent(doc, false);		
	}
	
	private void titleContext(final int position) {
		if (UserUtil.CURRENT_USER_ID == null || actionParam != ActionFactory.DEFAULT_PARAM) return;
		final List<String> item = new ArrayList<String>();
		for (int i = 0; i < words.length; i++)
			item.add("快捷回复:"+words[i]);
		item.add("涂鸦回复");
		item.add("转载到某版");
		item.add("转寄给某人");
		
		selectedDoc = (DocObject)getListAdapter().getItem(position);
		if (selectedDoc != null && selectedDoc.getAuthor().startsWith(UserUtil.CURRENT_USER_ID))
			item.add("编辑该贴");
		
		UIUtil.createContextItem(this, item, null, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch(which-words.length) {
					case CONTEXT_MENU_DRW:
						reDrawContent();
						break;
					case CONTEXT_MENU_CCC:
					case CONTEXT_MENU_FWD:
						showDialog(which-words.length);
						break;
					case CONTEXT_MENU_EDT:
						editOrReDocContent(selectedDoc, true);
						break;
					default:
						reDocContent(doc, item.get(which).toString().substring(5));
						break;
				}
			}
		}).setTitle("快速操作")
		.setIcon(R.drawable.ktip)
		.create().show();
	}

	private void switchHtmlMode() {
		UserUtil.HTML_MODE = !UserUtil.HTML_MODE;
		setListAdapter(new ContentListAdapter(list));
	}
	
	private void reDrawContent() {
		Intent intent = new Intent(this, ContentDrawingActivity.class);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ContentDetailActivity.tmp));
		startActivityForResult(intent, REQ_CONTENT_DRAW);
	}
	
	private void reDrawContentResult(final DocObject doc) {
		UIUtil.runActionInThread(ContentListActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				String m = ActionFactory.newInstance().newPostAction().sendAttFile(
					ContentDetailActivity.tmp, 
					doc.getBoard().isAttach()?doc.getBoard():HttpConfig.DEFAULT_UPLOAD_BOARD, "image/jpeg");
				DocObject d = ActionFactory.newInstance().newPostAction().inPostDoc(doc.getBoard(), doc);
				d.setContent(m + "\n" + d.getContent());
				ActionFactory.newInstance().newPostAction().sendPostDoc(d, doc, false, false, 0);
			}
			@Override
			public void actionFinish() {
				Toast.makeText(ContentListActivity.this, "涂鸦成功", Toast.LENGTH_SHORT).show();
				if (ContentDetailActivity.tmp.exists()) ContentDetailActivity.tmp.delete();
			}
		});
	}
	
	private void reDocContent(final DocObject doc, final String content) {
		UIUtil.runActionInThread(ContentListActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				DocObject d = ActionFactory.newInstance().newPostAction().inPostDoc(doc.getBoard(), doc);
				d.setContent(content + "\n" + d.getContent());
				ActionFactory.newInstance().newPostAction().sendPostDoc(d, doc, false, false, 0);
			}
			@Override
			public void actionFinish() {
				Toast.makeText(ContentListActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void fwdDocContent(final DocObject doc, final String user) {
		UIUtil.runActionInThread(ContentListActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newPostAction().fwdPostDoc(doc, user);
			}
			@Override
			public void actionFinish() {
				Toast.makeText(ContentListActivity.this, "转信成功", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void cccDocContent(final DocObject doc, final String board) {
		UIUtil.runActionInThread(ContentListActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newPostAction().cccPostDoc(doc, new BoardObject(board));
			}
			@Override
			public void actionFinish() {
				Toast.makeText(ContentListActivity.this, "转载成功", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void fetchDocContent(final int totalItemCount) {
		UIUtil.runActionInThread(ContentListActivity.this, new UIUtil.ActionInThread<DocObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (needClear)
					if (tag == null)
						bglist = ActionFactory.newInstance().newDocAction(actionParam).docContent(doc, titleMode);
					else
						bglist = ActionFactory.newInstance().newDocAction(actionParam).docContent(doc, titleMode, tag);
				else
					bglist = ActionFactory.newInstance().newDocAction(actionParam).docContent(list.get(totalItemCount - 1), titleMode, doc);
			}
			@Override
			public void actionFinish() {
				if (bglist == null) return;
				
				if (needClear && bglist.size() > 0) {
					//需清空，且获取到的数据，那么改写this.doc, clear list
					//对应，上下篇、上下楼、上下主题、主题展开、顶楼
					setThisDoc(bglist.get(0));
					list.clear();
				} 
				else if (!needClear && bglist.size() == 0) {
					//!needClear有两种情况，此处展开和获取更多
					//该提示主要为了提示获取更多
					//此时通过查看this.doc是否被改写过来判断，没有被改写过的，
					Toast.makeText(ContentListActivity.this, "别刷了，没有帖子了", Toast.LENGTH_SHORT).show();
				}
				
				//if (bglist.size() > 0) {
				list.addAll(bglist);
				((BaseAdapter) ContentListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
				//}
				
				tag = null;
			}
			
			@Override
			public void actionError() {
				scrollAction.rollback();
				tag = null;
			}
		});
	}
	
	//此处展开或者主题展开
	//改写this.doc，出错后无法回滚，展开后到titlemode无需回滚，不作处理
	//此处展开的时候，由于没有获取顶楼，所以顶楼的title将不正确
	private void fetchThisTitleContent(int fromItem) {
		if (doc.getGid() != null && doc.getGid().length() != 0) {
			titleMode = true;
			setThisDoc(new DocObject(doc.getGid(), doc.getGid(), doc.getTitle(), doc.getBoard()));
			scrollAction.refresh(fromItem);
		}
	}
	
	//获取顶楼
	//获取顶楼，改写this.doc，出错后无法回滚
	//无法获取顶楼的情况比较少，不做处理
	private void fetchFirstTitleContent() {
		if (doc.getGid() != null && doc.getGid().length() != 0) {
			setThisDoc(new DocObject(doc.getGid(), doc.getGid(), doc.getTitle(), doc.getBoard()));
			scrollAction.refresh();
		}
	}
	
	//上篇、下篇、上楼、下楼、上主题、下主题
	private void fetchContentFromTag(String tag) {
		this.tag = tag;
		scrollAction.refresh();
	}
	
	private void editOrReDocContent(DocObject doc, boolean edit) {
		this.selectedDoc = doc;
		Intent intent = new Intent(ContentListActivity.this, ContentDetailActivity.class);
		intent.putExtra("doc", doc);
		intent.putExtra("edit", edit);
		startActivityForResult(intent, REQ_CONTENT_DETAIL);
	}
	
	private ContentListScrollAction scrollAction = new ContentListScrollAction();
	private class ContentListScrollAction extends UIUtil.ActionInScroll {
		
		public ContentListScrollAction() {
			new UIUtil().super();
		}
		
		@Override
		protected void actionScrollPre(boolean auto) {
			if (auto) ContentListActivity.this.needClear = false;
		}
		
		@Override
		protected void actionClear() {
			ContentListActivity.this.needClear = true;
		}
		
		@Override
		protected void actionClear(int currentTotalItemCount) {
			ContentListActivity.this.needClear = false;
		}

		@Override
		protected void actionScroll(int totalItemCount) {
			fetchDocContent(totalItemCount);
		}

		@Override
		protected void actionRemove(Object o) {
			ContentListActivity.this.list.remove(o);
		}
		
	}
	
	private class ContentListAdapter extends BaseAdapter {

		private List<DocObject> list;

		public ContentListAdapter(List<DocObject> list) {
			this.list = list;
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(ContentListActivity.this)
						.inflate(SettingActivity.displayHd(ContentListActivity.this)?R.layout.content_item_hd:R.layout.content_item, parent, false);
			} else {
				tv = convertView;
			}
			TextView title = (TextView) tv.findViewById(R.id.conItemTitle);
			TextView author = (TextView) tv.findViewById(R.id.conItemAuthor);
			TextView date = (TextView) tv.findViewById(R.id.conItemDate);
			//WebView content1 = (WebView) tv.findViewById(R.id.conItemContent);
			TextView contentView = (TextView) tv.findViewById(R.id.conItemContent);
			View buttonBar1 = tv.findViewById(R.id.conButtonBar1);
			View buttonBar2 = tv.findViewById(R.id.conButtonBar2);
			View buttonBar3 = tv.findViewById(R.id.conButtonBar3);
			View titleBar = tv.findViewById(R.id.conTitleBar);

			title.setText(list.get(position).getTitle());
			author.setText(list.get(position).getAuthor());
			date.setText(list.get(position).getDate());
			String content = list.get(position).getContent();
			
			if (!titleMode && list.size() == 1 && !list.get(0).isSticky() && ActionFactory.DEFAULT_PARAM == actionParam) {
				configButtonBar1(tv);
				configButtonBar3(tv);
				buttonBar1.setVisibility(View.VISIBLE);
				buttonBar2.setVisibility(View.GONE);
				buttonBar3.setVisibility(View.VISIBLE);
			} else if (titleMode && list.size()-1 == position && ActionFactory.DEFAULT_PARAM == actionParam) {
				configButtonBar2(tv);
				buttonBar1.setVisibility(View.GONE);
				buttonBar2.setVisibility(View.VISIBLE);
				buttonBar3.setVisibility(View.GONE);
			} else {
				buttonBar1.setVisibility(View.GONE);
				buttonBar2.setVisibility(View.GONE);
				buttonBar3.setVisibility(View.GONE);
			}
			
			titleBar.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View view) {
					titleContext(position);
					return true;
				}
			});
			
			titleBar.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					titleClick(position);
				}
			});

			ContentListRender render = new ContentListRender(
					ContentListActivity.this, renderColorContent, UserUtil.HTML_MODE, 
					(notitleReImage && !titleMode && list.size() == 1), qmdImage, 
					contentView, list.get(position));
			render.render(content);
			return tv;
		}
		//非主题模式的展开按钮
		//全部展开，clear，刷新到顶部，重置this.doc
		//此处展开，不clear，从底部开始获取该主题的更多文章，重置this.doc
		private void configButtonBar1(View tv) {
			Button b1 = (Button)tv.findViewById(R.id.conTitleButton1);
			b1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchThisTitleContent(0);
				}				
			});
			Button b2 = (Button)tv.findViewById(R.id.conTitleButton2);
			b2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchThisTitleContent(1);
				}				
			});
		}
		//主题模式的获取更多按钮
		//不clear，从底部开始获取该主题的更多文章，不重置this.doc
		private void configButtonBar2(View tv) {
			Button b1 = (Button)tv.findViewById(R.id.titleButton1);
			b1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					scrollAction.refresh(list.size());
				}				
			});
		}
		//非主题模式的上篇、下篇、上楼、下楼、顶楼
		//clear，刷新到顶部，重置this.doc
		private void configButtonBar3(View tv) {
			Button b1 = (Button)tv.findViewById(R.id.conButton1);
			b1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchContentFromTag("a=p");
				}				
			});
			Button b2 = (Button)tv.findViewById(R.id.conButton2);
			b2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchContentFromTag("a=n");
				}				
			});
			Button b3 = (Button)tv.findViewById(R.id.conButton3);
			b3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchContentFromTag("a=b");
				}				
			});
			Button b4 = (Button)tv.findViewById(R.id.conButton4);
			b4.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchContentFromTag("a=a");
				}				
			});
			Button b5 = (Button)tv.findViewById(R.id.conButton5);
			b5.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					fetchFirstTitleContent();
				}				
			});
		}
	}
}