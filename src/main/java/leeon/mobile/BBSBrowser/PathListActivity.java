package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.BBSPathAction;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.PathObject;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PathListActivity extends ListActivity {
	
	private List<PathObject> list;
	private PathObject current;
	private int stackTop = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getListView().setDividerHeight(0);
		this.getListView().setBackgroundResource(R.color.login_background);

		final BoardObject board = (BoardObject) this.getIntent().getSerializableExtra("board");
		if (board != null) {
			this.setTitle("["+board.getName()+"]版精华区");
		} else {
			this.setTitle("日月光华精华区");
		}
		
		list = new ArrayList<PathObject>();
		UIUtil.runActionInThread(PathListActivity.this, new UIUtil.ActionInThread<PathObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (board != null) {
					addList(ActionFactory.newInstance().newPathAction().fetchPath(board));
					current = list.get(0).getParent();
				} else {
					addList(ActionFactory.newInstance().newPathAction().fetchPath(BBSPathAction.root));
					current = BBSPathAction.root;
				}
			}
			
			@Override
			public void actionFinish() {
				setListAdapter(new PathListAdapter(list));
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
				&& keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//为兼容2.0以下，由app调用onbackpressed
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		if (stackTop == 0)
			finish();
		else {
			fetchPathChildren(current.getParent(), true);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		PathObject p = list.get(position);
		if ("d".equals(p.getType())) {
			fetchPathChildren(p, false);
		} else if ("f".equals(p.getType())) {
			fetchPathContent(p);
		}
	}
	
	private void fetchPathChildren(final PathObject path, final boolean up) {
		UIUtil.runActionInThread(PathListActivity.this, new UIUtil.ActionInThread<PathObject>() {
			private List<PathObject> l;
			
			@Override
			public void action() throws NetworkException, ContentException {
				l = ActionFactory.newInstance().newPathAction().fetchPath(path);
			}
			@Override
			public void actionFinish() {
				if (l == null) {
					Toast.makeText(PathListActivity.this, "别点那么快!", Toast.LENGTH_LONG).show();
				} else if (l.size() == 0)
					Toast.makeText(PathListActivity.this, "空目录,别去了!", Toast.LENGTH_LONG).show();
				else {
					current = path;
					if (up) stackTop--; else stackTop++;
					addList(l);
					((BaseAdapter)PathListActivity.this.getListAdapter()).notifyDataSetChanged();
				}
			}
		});
	}
	
	private void fetchPathContent(final PathObject path) {
		UIUtil.runActionInThread(PathListActivity.this, new UIUtil.ActionInThread<PathObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newPathAction().fetchContent(path);
			}
			@Override
			public void actionFinish() {
				Intent intent = new Intent(PathListActivity.this, PathContentActivity.class);
				intent.putExtra("content", path.getContent());
				startActivity(intent);
			}
		});
	}
	
	private void addList(List<PathObject> l) {
		list.clear();
		list.addAll(l);
	}

	private class PathListAdapter extends BaseAdapter {

		private List<PathObject> list;

		public PathListAdapter(List<PathObject> list) {
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
				tv = LayoutInflater.from(PathListActivity.this)
					.inflate(SettingActivity.displayHd(PathListActivity.this)?R.layout.path_item_hd:R.layout.path_item, parent, false);
			} else {
				tv = convertView;
			}
			
			TextView t1 = (TextView)tv.findViewById(R.id.pathItemName);
			TextView t2 = (TextView)tv.findViewById(R.id.pathItemAuthor);
			TextView t3 = (TextView)tv.findViewById(R.id.pathItemTime);
			
			String author = list.get(position).getAuthor();
			author = author!=null&&author.length()!=0?author:" ";
			
			t1.setText(list.get(position).getName());
			t3.setText(list.get(position).getTime());
			t2.setText(author);
			if ("d".equals(list.get(position).getType())) {
				t2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_open, 0, 0, 0);
			} else {
				t2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filenew, 0, 0, 0);
			}
			return tv;
		}
	}
}