package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BlockObject;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BlockView {
	
	private Context context;
	private int actionParam = ActionFactory.DEFAULT_PARAM;
	
	public BlockView(Context context) {
		this.context = context;
	}
	
	public BlockView(Context context, int actionParam) {
		this.context = context;
		this.actionParam = actionParam;
	}
	
	/**
	 * 返回所有版面
	 */
	public View createAllBoardView() {		
		final ListView blockList = new ListView(context);
		blockList.setDividerHeight(0);
		blockList.setAdapter(new BlockListAdapter(getAllBoard()));
		blockList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				BlockObject block = getAllBoard().get(position);
				Intent intent = new Intent(context, BoardListActivity.class);
				intent.putExtra("block", block);
				intent.putExtra("action", actionParam);
				context.startActivity(intent);
			}
			
		});
		return blockList;
	}
	
	private List<BlockObject> getAllBoard() {
		try {
			return ActionFactory.newInstance().newBoardAction(actionParam).allBlock();
		} catch (NetworkException e) {
			Log.e("blockview", "run all board error", e);
			return new ArrayList<BlockObject>();
		}
	}
	
    private class BlockListAdapter extends BaseAdapter {
    	
    	private List<BlockObject> list;
    	
		public BlockListAdapter(List<BlockObject> list) {
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
				tv =  LayoutInflater.from(BlockView.this.context)
					.inflate(SettingActivity.displayHd(BlockView.this.context)?R.layout.board_list_hd:R.layout.board_list, parent, false);
			} else {
				tv =  convertView;
			}
			TextView l = (TextView)tv.findViewById(R.id.boardListItem);
			l.setText(list.get(position).getName());
			l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder, 0, 0, 0);
			l.setCompoundDrawablePadding(5);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			if (position%2 == 1)
				tv.setBackgroundResource(R.drawable.welcome_selector1);
			else
				tv.setBackgroundResource(R.drawable.welcome_selector2);
			return tv;
		}
	}
}
