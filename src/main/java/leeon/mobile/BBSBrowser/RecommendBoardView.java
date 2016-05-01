package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class RecommendBoardView {
	
	private Context context;
	private int actionParam = ActionFactory.DEFAULT_PARAM;;
	
	public RecommendBoardView(Context context) {
		this.context = context;
	}
	
	public RecommendBoardView(Context context, int actionParam) {
		this.context = context;
		this.actionParam = actionParam;
	}
	
	/**
	 * 返回推荐版面
	 */
	public View createRecommendBoardView() {
		final ExpandableListView recommendBoardList = new ExpandableListView(context);
		recommendBoardList.setDividerHeight(0);
		recommendBoardList.setGroupIndicator(context.getResources().getDrawable(R.drawable.recommend_indicator_selector));
		recommendBoardList.setAdapter(new RecommendBoardListAdapter(getAllBoard()));
		recommendBoardList.setOnChildClickListener(new OnChildClickListener() {

			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {				
				BoardObject board = getAllBoard().get(groupPosition).getRecommendBoardList().get(childPosition);
				Intent intent = new Intent(context, DocListActivity.class);
				intent.putExtra("board", board);
				intent.putExtra("action", actionParam);
				context.startActivity(intent);
				return false;
			}
			
		});
		return recommendBoardList;		
	}
	
	private List<BlockObject> getAllBoard() {
		try {
			return ActionFactory.newInstance().newBoardAction(actionParam).allBlock();
		} catch (NetworkException e) {
			Log.e("blockview", "run all board error", e);
			return new ArrayList<BlockObject>();
		}
	}
	
	private class RecommendBoardListAdapter extends BaseExpandableListAdapter {

		private List<BlockObject> list;
        
        public RecommendBoardListAdapter(List<BlockObject> list) {
        	this.list = list;
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            return list.get(groupPosition).getRecommendBoardList().get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return list.get(groupPosition).getRecommendBoardList().size();
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {            
        	View tv;
			if (convertView == null) {
				tv =  LayoutInflater.from(RecommendBoardView.this.context)
						.inflate(SettingActivity.displayHd(RecommendBoardView.this.context)?R.layout.board_list_hd:R.layout.board_list, parent, false);
			} else {
				tv =  convertView;
			}
			TextView l = (TextView)tv.findViewById(R.id.boardListItem);        	
            BoardObject b = list.get(groupPosition).getRecommendBoardList().get(childPosition);
            l.setText("["+b.getName()+"]"+b.getCh());
            l.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_on, 0, 0, 0);
            l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			if ((groupPosition+childPosition)%2 == 1)
				tv.setBackgroundResource(R.drawable.welcome_selector2);
			else
				tv.setBackgroundResource(R.drawable.welcome_selector1);
            return tv;
        }

        public Object getGroup(int groupPosition) {
            return list.get(groupPosition);
        }

        public int getGroupCount() {
            return list.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
        	View tv;
			if (convertView == null) {
				tv =  LayoutInflater.from(RecommendBoardView.this.context)
						.inflate(SettingActivity.displayHd(RecommendBoardView.this.context)?R.layout.board_list_hd:R.layout.board_list, parent, false);
			} else {
				tv =  convertView;
			}
			TextView l = (TextView)tv.findViewById(R.id.boardListItem);
            l.setText(list.get(groupPosition).getName());
            l.setPadding(50, 5, 0, 5);
            l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            if (groupPosition%2 == 1)
            	tv.setBackgroundResource(R.drawable.welcome_selector1);
			else
				tv.setBackgroundResource(R.drawable.welcome_selector2);
            return tv;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
}
