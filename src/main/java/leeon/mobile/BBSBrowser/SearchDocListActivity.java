package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.BBSDocAction;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchDocListActivity extends GestureListActivity {
			
	private List<DocObject> list;
	private DocObject selectedDoc;
	
	private BoardObject board;
	private String t1;
	private String t2;
	private String t3;
	private String author;
	private int limit = 7;
	private boolean mark = false;
	private boolean nore = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getListView().setDividerHeight(0);
		this.getListView().setBackgroundResource(R.color.app_background_doc);

		board = (BoardObject)this.getIntent().getSerializableExtra("board");
		
		list = new ArrayList<DocObject>();		
		setListAdapter(new DocListAdapter(list));
		refreshTitle();
		
		searchDoc();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		selectedDoc = list.get(position);
		Intent intent = new Intent(SearchDocListActivity.this, ContentListActivity.class);
		intent.putExtra("doc", selectedDoc);
		startActivityForResult(intent, REQ_CONTENT);
	}
	
	private static final int REQ_CONTENT  = 0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CONTENT) {
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
			((BaseAdapter)SearchDocListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
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
		menu.add(0, 1, 1, "重新搜索").setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 2, 2, "刷新结果").setIcon(android.R.drawable.ic_menu_rotate);
        return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1: searchDoc();break;
			case 2: refreshDoc();break;
			default: break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("search".equals(actionName)) {
			searchDoc();
		} else if ("back".equals(actionName)) {
			finish();
		} else if ("refresh".equals(actionName)) {
			refreshDoc();
		}
	}
	
	private void searchDoc() {
		View view = LayoutInflater.from(this).inflate(R.layout.doc_search_dialog, null);
		final EditText et1 = (EditText)view.findViewById(R.id.t1);
		final EditText et3 = (EditText)view.findViewById(R.id.t3);
		final EditText eau = (EditText)view.findViewById(R.id.author);
		final EditText eli = (EditText)view.findViewById(R.id.limit);
		final CheckBox cm = (CheckBox)view.findViewById(R.id.mark);
		final CheckBox cn = (CheckBox)view.findViewById(R.id.nore);
		et1.setText(t1);
		et3.setText(t3);
		eau.setText(author);
		eli.setText(String.valueOf(limit));
		cm.setChecked(mark);
		cn.setChecked(nore);		
		new AlertDialog.Builder(this)
			.setTitle("搜索版面:["+board.getName()+"]"+board.getCh())
			.setView(view)
			.setCancelable(false)
			.setPositiveButton("搜索", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					t1 = et1.getText().toString();
					t3 = et3.getText().toString();
					author= eau.getText().toString();
					mark = cm.isChecked();
					nore = cn.isChecked();
					limit = Integer.parseInt(eli.getText().toString());
					refreshDoc();
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SearchDocListActivity.this.finish();
				}
		}).create().show();
	}
		
	private void refreshDoc() {		
		UIUtil.runActionInThread(SearchDocListActivity.this, new UIUtil.ActionInThread<DocObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (t1 == null && t2 == null && t3 == null && author == null) return;
				bglist = BBSDocAction.findDoc(board, t1, t2, t3, author, limit, mark, nore);
			}
			@Override
			public void actionFinish() {
				if (bglist != null) {
					addDocList(bglist);
					((BaseAdapter)SearchDocListActivity.this.getListView().getAdapter()).notifyDataSetChanged();
					SearchDocListActivity.this.refreshTitle();
				}
			}
		});
	}
	
	private void refreshTitle() {
		this.setTitle("["+board.getName()+"]"+board.getCh()+",搜索结果:("+list.size()+")");
	}
	
	private void addDocList(List<DocObject> list1) {
		list.clear();
		for (int i = list1.size()-1; i >= 0; i --) {
			list.add(list1.get(i));
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
				tv = LayoutInflater.from(SearchDocListActivity.this)
						.inflate(SettingActivity.displayHd(SearchDocListActivity.this)?R.layout.doc_item_hd:R.layout.doc_item, parent, false);
			} else {
				tv = convertView;
			}
			TextView title = (TextView)tv.findViewById(R.id.docItemTitle);
			TextView author = (TextView)tv.findViewById(R.id.docItemAuthor);
			TextView date = (TextView)tv.findViewById(R.id.docItemDate);
			TextView status = (TextView)tv.findViewById(R.id.docItemStatus);
			
			title.setText(list.get(position).getTitle());
			author.setText(list.get(position).getAuthor());
			date.setText(list.get(position).getDate());
			status.setText(list.get(position).getStatus());			
			return tv;
		}
	}
}