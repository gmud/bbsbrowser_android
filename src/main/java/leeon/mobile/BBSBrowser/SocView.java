package leeon.mobile.BBSBrowser;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SocView {
	
	private Context context;
	
	public SocView(Context context) {
		this.context = context;
	}
	
	/**
	 * 返回所有版面
	 */
	public View createSocView() {		
		final ListView socList = new ListView(context);
		socList.setDividerHeight(0);
		socList.setAdapter(new SocListAdapter(new String[]{"饮水思源", "燕曦BBS"}));
		socList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				if (position == 0) {
					context.startActivity(new Intent(context, OtherBoardTabActivity.class)
						.putExtra("action", ActionFactory.SJTU_PARAM));
				} else if (position == 1) {
					context.startActivity(new Intent(context, OtherBoardTabActivity.class)
						.putExtra("action", ActionFactory.YANXI_PARAM));
				}
			}
			
		});
		return socList;
	}
	
    private class SocListAdapter extends BaseAdapter {
    	
    	private String[] list;
    	
		public SocListAdapter(String[] list) {
			this.list = list;
		}

		public int getCount() {
			return list.length;
		}

		public Object getItem(int position) {
			return list[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv =  LayoutInflater.from(SocView.this.context).inflate(android.R.layout.simple_list_item_1, parent, false);
			} else {
				tv =  convertView;
			}
			TextView l = (TextView)tv.findViewById(android.R.id.text1);
			l.setText(list[position]);
			l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.other_icon, 0, 0, 0);
			l.setCompoundDrawablePadding(5);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			l.setTextColor(SocView.this.context.getResources().getColor(R.color.welcome_text));

			tv.setBackgroundResource(R.drawable.welcome_selector1);
			tv.setPadding(5, 0, 0, 0);
			return tv;
		}
	}
}
