package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.DocObject;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TopTenDocView extends BaseGestureView {
	
	private Context context;
	
	private ListView topTenList;
	public static List<DocObject> top10List = new ArrayList<DocObject>();
	
	public TopTenDocView(Context context) {
		super(context);
		this.context = context;
	}
	
	public void refresh() {
		UIUtil.runActionInThread(context, new UIUtil.ActionInThread<DocObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				bglist = ActionFactory.newInstance().newDocAction().topTenDocList();
			}
			@Override
			public void actionFinish() {
				if (bglist == null) return;
				top10List.clear();
				top10List.addAll(bglist);
				((BaseAdapter)topTenList.getAdapter()).notifyDataSetChanged();
			}
		});		
	}
	
	/**
	 * 返回top ten版面
	 */
	public View createTopTenDocView() {
		topTenList = new ListView(context);
		topTenList.setDividerHeight(0);
		topTenList.setAdapter(new TopTenListAdapter(top10List));
		topTenList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				DocObject doc = top10List.get(position);
				Intent intent = new Intent(context, ContentListActivity.class);
				intent.putExtra("doc", doc);
				intent.putExtra("title", true);
				context.startActivity(intent);
			}
		});
		refresh();
		
		this.addListView(topTenList);
		return this.gestureOverlayView;
	}
	
	/**
	 * 识别手势
	 */
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("refresh".equals(actionName)) {
			refresh();
		}
	}
	
    private class TopTenListAdapter extends BaseAdapter {
    	
    	private List<DocObject> list;
    	
		public TopTenListAdapter(List<DocObject> list) {
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
				tv =  LayoutInflater.from(TopTenDocView.this.context)
						.inflate(SettingActivity.displayHd(TopTenDocView.this.context)?R.layout.top10_doc_item_hd:R.layout.top10_doc_item, parent, false);
			} else {
				tv =  convertView;
			}
			TextView title = (TextView)tv.findViewById(R.id.top10ItemTitle);
			TextView author = (TextView)tv.findViewById(R.id.top10ItemAuthor);
			TextView docNumber = (TextView)tv.findViewById(R.id.top10ItemDocNumber);
			TextView boardName = (TextView)tv.findViewById(R.id.top10ItemBoard);
			
			
			title.setText(list.get(position).getTitle());
			author.setText(list.get(position).getAuthor());
			docNumber.setText(list.get(position).getDocNumber());
			boardName.setText(list.get(position).getBoard().getName());
			return tv;
		}
	}
}
