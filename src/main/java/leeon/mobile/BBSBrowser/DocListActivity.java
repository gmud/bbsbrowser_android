package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DocListActivity extends GestureListActivity {
		
	private int stickyCount = 0;	
	private List<DocObject> list;
	private DocObject selectedDoc;
	
	private BoardObject board;
	private int actionParam;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getListView().setDividerHeight(0);
		this.getListView().setBackgroundResource(R.color.app_background_doc);

		board = (BoardObject)this.getIntent().getSerializableExtra("board");
		actionParam = this.getIntent().getIntExtra("action", ActionFactory.DEFAULT_PARAM);
		
		list = new ArrayList<DocObject>();		
		setListAdapter(new DocListAdapter(list));
		scrollAction.addActionWhenScroll(getListView());
		
		refreshDoc();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		selectedDoc = list.get(position);
		Intent intent = new Intent(DocListActivity.this, ContentListActivity.class);
		intent.putExtra("doc", selectedDoc);
		intent.putExtra("action", actionParam);
		startActivityForResult(intent, REQ_CONTENT);
	}
	
	private static final int REQ_CONTENT  = 0;
	private static final int REQ_POST  = 1;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CONTENT) {
			if (resultCode == Activity.RESULT_OK) {
				if (ContentDetailActivity.POST_ACTION_DEL.equals(data.getAction())) {
					scrollAction.remove(selectedDoc);
				}
			}
			if (UserUtil.CURRENT_USER_ID != null && selectedDoc != null) {
				if ("+".equals(selectedDoc.getStatus())) {
					selectedDoc.setStatus(" ");
				} else if ("M".equals(selectedDoc.getStatus())) {
					selectedDoc.setStatus("m");
				} else if ("G".equals(selectedDoc.getStatus())) {
					selectedDoc.setStatus("g");
				} else if ("B".equals(selectedDoc.getStatus())) {
					selectedDoc.setStatus("b");
				}
			}
			((BaseAdapter)DocListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
		} else if (requestCode == REQ_POST) {
		}
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
		menu.add(0, 1, 3, (UserUtil.TITLE_MODE?"一般模式":"主题模式")).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, 2, 4, ("加入收藏")).setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, 3, 1, ("发表大作")).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, 4, 2, ("刷新版面")).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 5, 5, ("搜索版面")).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 6, 6, ("浏览精华")).setIcon(android.R.drawable.ic_menu_view);
        return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		if (actionParam == ActionFactory.DEFAULT_PARAM) {
			if (UserUtil.CURRENT_USER_ID != null) { 
				menu.findItem(2).setEnabled(true);
				menu.findItem(3).setEnabled(true);
				menu.findItem(5).setEnabled(true);
			} else {
				menu.findItem(2).setEnabled(false);
				menu.findItem(3).setEnabled(false);
				menu.findItem(5).setEnabled(false);
			}
			menu.findItem(6).setEnabled(true);
		} else {
			menu.findItem(2).setEnabled(true);
			menu.findItem(3).setEnabled(false);
			menu.findItem(5).setEnabled(false);
			menu.findItem(6).setEnabled(false);
		}
		menu.findItem(1).setTitle((UserUtil.TITLE_MODE?"一般模式":"主题模式"));
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1: switchTitleMode();break;
			case 2: addToFav();break;
			case 3: showDialog(DIALOG_NEWDOC);break;
			case 4: refreshDoc();break;
			case 5: searchDoc();break;
			case 6: goPath();break;
			default: break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private static final int DOC_NEW = 0;
	private static final int DIALOG_NEWDOC = 0;
	private static final String[] DIALOG_NEWDOC_ITEM = {"自己写"};
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_NEWDOC:
			return new AlertDialog.Builder(this)
				.setTitle("发表大作")
				.setIcon(android.R.drawable.ic_menu_add)
				.setAdapter(new NewdocListAdapter(DIALOG_NEWDOC_ITEM), new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DOC_NEW:
								newPost();
								break;
							default:
								return;
						}
					}
			}).create();
		default:
			return null;
		}
	}	
	
	@Override
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("add".equals(actionName)) {
			if (UserUtil.CURRENT_USER_ID == null) return; 
			newPost();
		} else if ("check".equals(actionName)) {
			if (UserUtil.CURRENT_USER_ID == null) return;
			new AlertDialog.Builder(this).setTitle("管理收藏")
				.setMessage("将["+board.getName()+"]加入收藏")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						addToFav();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
			}).create().show();
		} else if ("title".equals(actionName)) {
			switchTitleMode();
		} else if ("refresh".equals(actionName)) {
			refreshDoc();
		} else if ("search".equals(actionName)) {
			if (UserUtil.CURRENT_USER_ID == null) return;
			searchDoc();
		} else if ("back".equals(actionName)) {
			finish();
		}
	}

	private void switchTitleMode() {
		UserUtil.TITLE_MODE = !UserUtil.TITLE_MODE;
		refreshDoc();
	}
	
	private void addToFav() {
		//非fdu的加入收藏
		if (actionParam != ActionFactory.DEFAULT_PARAM) {
			UIUtil.runActionInThread(DocListActivity.this, new UIUtil.ActionInThread<DocObject>() {
				@Override
				public void action() throws NetworkException, ContentException {
					List<BoardObject> list = UserUtil.getOtherFavList(DocListActivity.this, actionParam);
					list.add(board);
					UserUtil.saveOtherFavList(DocListActivity.this, list, actionParam);
				}
				@Override
				public void actionFinish() {
					Toast.makeText(DocListActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}
		
		UIUtil.runActionInThread(DocListActivity.this, new UIUtil.ActionInThread<DocObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newLogAction().addFavBoard(board);
			}
			@Override
			public void actionFinish() {
				Toast.makeText(DocListActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
			}
		});
			
	}
	
	private void newPost() {
		Intent intent = new Intent(DocListActivity.this, ContentDetailActivity.class);
		intent.putExtra("board", board);
		startActivityForResult(intent, REQ_POST);
	}
	
	private void searchDoc() {
		Intent intent = new Intent(DocListActivity.this, SearchDocListActivity.class);
		intent.putExtra("board", board);
		startActivity(intent);
	}
	
	private void goPath() {
		Intent intent = new Intent(DocListActivity.this, PathListActivity.class);
		intent.putExtra("board", board);
		startActivity(intent);
	}
	
	private void refreshDoc() {		
		if (board != null) {
			scrollAction.refresh();
		}
	}
	
	private void refreshTitle() {
		this.setTitle("["+board.getName()+"]"+board.getCh()+":("+list.size()+"/"+board.getTotal()+")");
	}
	
	private void fetchBoardDoc(final int totalItemCount) {
		UIUtil.runActionInThread(DocListActivity.this, new UIUtil.ActionInThread<DocObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				//modify成调用统一接口，传入stickyCOunt和total
				bglist = ActionFactory.newInstance().newDocAction(actionParam).boardDoc(board, UserUtil.TITLE_MODE, totalItemCount, stickyCount);
			}
			@Override
			public void actionFinish() {
				if (bglist == null) return;
				addDocList(bglist);
				((BaseAdapter)DocListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
				DocListActivity.this.refreshTitle();
			}
			
			@Override
			public void actionError() {
				scrollAction.rollback();
			}
		});
	}
	
	//end表示从倒数第几个list1开始提取数据到list中
	//end在一般情况下用0，在提取到第一页的时候，
	//可能出现第一页和上一页的数据重复，这时候给end一个正整数用来表示从倒数第几个值开始提取，避免重复
	//modify 去掉endFromLast，在实现中去掉多余的文章，而不是在界面解决
	private void addDocList(List<DocObject> list1) {
		for (int i = list1.size()-1; i >= 0; i --) {
			if (list.size() < 20 || !list1.get(i).isSticky()) {
				list.add(list1.get(i));
				if (list1.get(i).isSticky()) stickyCount++;
			}
		}
	}
	
	private DocListScrollAction scrollAction = new DocListScrollAction();
	private class DocListScrollAction extends UIUtil.ActionInScroll {
		
		public DocListScrollAction() {
			new UIUtil().super();
		}
		
		@Override
		protected void actionClear() {
			DocListActivity.this.list.clear();
			DocListActivity.this.stickyCount = 0;
		}

		@Override
		protected void actionScroll(int totalItemCount) {
			fetchBoardDoc(totalItemCount);
		}

		@Override
		protected void actionRemove(Object o) {
			DocListActivity.this.list.remove(o);
		}
		
	}
	

	private class DocListAdapter extends BaseAdapter {

		private List<DocObject> list;

		public DocListAdapter(List<DocObject> list) {
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

		public View getView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(DocListActivity.this)
						.inflate(SettingActivity.displayHd(DocListActivity.this)?R.layout.doc_item_hd:R.layout.doc_item, parent, false);
			} else {
				tv = convertView;
			}
			TextView title = (TextView)tv.findViewById(R.id.docItemTitle);
			TextView author = (TextView)tv.findViewById(R.id.docItemAuthor);
			TextView date = (TextView)tv.findViewById(R.id.docItemDate);
			TextView status = (TextView)tv.findViewById(R.id.docItemStatus);
			TextView sticky = (TextView)tv.findViewById(R.id.docItemSticky);
			
			title.setText(list.get(position).getTitle());
			author.setText(list.get(position).getAuthor());
			date.setText(list.get(position).getDate());
			status.setText(list.get(position).getStatus());
			sticky.setText(list.get(position).isSticky()?"置顶":String.valueOf(list.get(position).getBoard().getTotal()-position+stickyCount));
			
			return tv;
		}
	}
	
	private class NewdocListAdapter extends BaseAdapter {
		private String[] item;
		
		public NewdocListAdapter(String[] item) {
			this.item = item;
		}
		
		public int getCount() {
			return item.length;
		}

		public Object getItem(int position) {
			return item[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(DocListActivity.this)
						.inflate(android.R.layout.simple_list_item_1, parent, false);
			} else {
				tv = convertView;
			}
			TextView l = (TextView)tv.findViewById(android.R.id.text1);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			l.setCompoundDrawablePadding(5);
			l.setText(item[position]);
			l.setTextColor(Color.BLACK);
			
			if (position == 0) {
				l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.krita, 0, 0, 0);
			//} else if (position == 1) {
			//	l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sinaicon, 0, 0, 0);
			//} else if (position == 2) {
			//	l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kaixin_icon, 0, 0, 0);
			}
			
			return tv;
		}
	}
}