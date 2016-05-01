package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BoardListActivity extends ListActivity {
	
	private List<BoardObject> list;
	
	private int actionParam;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getListView().setDividerHeight(0);
		this.getListView().setBackgroundResource(R.color.app_background);
		
		final BlockObject block = (BlockObject)this.getIntent().getSerializableExtra("block");
		final BoardObject board = (BoardObject)this.getIntent().getSerializableExtra("board");
		actionParam = this.getIntent().getIntExtra("action", ActionFactory.DEFAULT_PARAM);
		
		list = new ArrayList<BoardObject>();
		
		if (block != null) {
			this.setTitle("讨论区:"+block.getName());
		} else if (board != null){
			this.setTitle("讨论区:["+board.getName()+"]"+board.getCh());
		}

		UIUtil.runActionInThread(BoardListActivity.this, new UIUtil.ActionInThread<BoardObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (block != null) {
					ActionFactory.newInstance().newBoardAction(actionParam).blockBoard(block);
					list = block.getAllBoardList();
				} else if (board != null) {
					list = board.getChildBoardList();
				}
			}
			@Override
			public void actionFinish() {
				setListAdapter(new BoardListAdapter(list));
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		BoardObject board = list.get(position);
		if (board.isDir()) {
			Intent intent = new Intent(BoardListActivity.this, BoardListActivity.class);
			intent.putExtra("board", board);
			intent.putExtra("action", actionParam);
			BoardListActivity.this.startActivity(intent);
		} else {
			Intent intent = new Intent(BoardListActivity.this, DocListActivity.class);
			intent.putExtra("board", board);
			intent.putExtra("action", actionParam);
			BoardListActivity.this.startActivity(intent);
		}
	}
	

	private class BoardListAdapter extends BaseAdapter {

		private List<BoardObject> list;

		public BoardListAdapter(List<BoardObject> list) {
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
				tv = LayoutInflater.from(BoardListActivity.this)
					.inflate(SettingActivity.displayHd(BoardListActivity.this)?R.layout.board_item_hd:R.layout.board_item, parent, false);
			} else {
				tv = convertView;
			}
			
			TextView ch = (TextView)tv.findViewById(R.id.boardItemCh);
			TextView docNumber = (TextView)tv.findViewById(R.id.boardItemDocNumber);
			TextView master = (TextView)tv.findViewById(R.id.boardItemMaster);
			TextView name = (TextView)tv.findViewById(R.id.boardItemName);
			TextView type = (TextView)tv.findViewById(R.id.boardItemType);
			
			String mst = list.get(position).getMaster();
			mst = mst!=null&&mst.length()!=0?mst:" ";
			
			ch.setText(list.get(position).getCh());
			docNumber.setText(list.get(position).getDocNumber());
			master.setText(mst);
			name.setText(list.get(position).getName());
			type.setText(list.get(position).getType());
			
			if (list.get(position).isDir())
				master.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_open, 0, 0, 0);
			
			return tv;
		}
	}
}