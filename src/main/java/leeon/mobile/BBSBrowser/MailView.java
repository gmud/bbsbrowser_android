package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.models.MailObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MailView extends BaseGestureView {
	
	private Context context;
	
	private ListView mailListView;
	private List<MailObject> mailList = new ArrayList<MailObject>();
	private MailObject mailDetail;
	
	public MailView(Context context) {
		super(context);
		this.context = context;
	}
	
	public void refresh() {
		if (UserUtil.CURRENT_USER_ID != null) {
			scrollAction.refresh();
		}
	}
	
	/**
	 * 返回信件列表
	 */
	public View createMailListView() {		
		mailListView = new ListView(context);		
		mailListView.setDividerHeight(0);		
		mailListView.setAdapter(new MailListAdapter(mailList));
		mailListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				mailDetail = mailList.get(position);
				Intent intent = new Intent(context, MailDetailActivity.class);
				intent.putExtra("mail", mailDetail);
				((Activity)context).startActivityForResult(intent, BoardTabActivity.REQ_MAIL_DETAIL);
			}
			
		});
		scrollAction.addActionWhenScroll(mailListView);
		refresh();
		
		this.addListView(mailListView);
		return this.gestureOverlayView;
	}
	

	/**
	 * 识别手势
	 */
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("add".equals(actionName)) {
			newMail();
		} else if ("minus".equals(actionName)) {
			if (position >= 0 && position < mailList.size()) {
				new AlertDialog.Builder(context).setTitle("管理邮件")
					.setMessage("删除不用的邮件["+mailList.get(position).getTitle()+"]")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							delMail(mailList.get(position));
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
				}).create().show();
			}
		} else if ("refresh".equals(actionName)) {
			refresh();
		}
	}
	
	void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (MailDetailActivity.MAIL_ACTION_RE.equals(data.getAction())) {
				if ("m".equals(mailDetail.getStatus()))
					mailDetail.setStatus("b");
				else
					mailDetail.setStatus("r");
			} else if (MailDetailActivity.MAIL_ACTION_DEL.equals(data.getAction())) {
				//scrollAction.remove(mailDetail);
				refresh();//删除后邮件总数等信息会发生变化，所以重新刷新
				return;
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			if ("+".equals(mailDetail.getStatus()))
				mailDetail.setStatus(" ");
		}
		((BaseAdapter)mailListView.getAdapter()).notifyDataSetChanged();
	}
	
	void newMail() {
		Intent intent = new Intent(context, MailDetailActivity.class);
		context.startActivity(intent);
	}
	
	private void addMailList(List<MailObject> list) {
//		for (int i = list.size()-1; i >= 0; i --) {
//			mailList.add(list.get(i));
//		}
		mailList.addAll(list);
	}
	
	private void fetchMailList(final int totalItemCount) {
		UIUtil.runActionInThread(context, new UIUtil.ActionInThread<MailObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (totalItemCount == 0)
					bglist = ActionFactory.newInstance().newMailAction().mailList();
				else
					bglist = ActionFactory.newInstance().newMailAction().mailList(
								ActionFactory.newInstance().newMailAction().totalMailCount()
								-totalItemCount-HttpConfig.BBS_PAGE_SIZE+1);
			}
			@Override
			public void actionFinish() {
				if (bglist != null) {
					addMailList(bglist);
					((BaseAdapter)mailListView.getAdapter()).notifyDataSetChanged();
				}
			}
		});		
	}
	
	private void delMail(final MailObject mail) {
		UIUtil.runActionInThread(context, new UIUtil.ActionInThread<MailObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newMailAction().delMail(mail);
			}
			@Override
			public void actionFinish() {
				Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
				refresh();//删除后邮件总数等信息会发生变化，所以重新刷新
				//scrollAction.remove(mail);
				//((BaseAdapter)mailListView.getAdapter()).notifyDataSetChanged();
			}
		});
	}
	
	private MailListScrollAction scrollAction = new MailListScrollAction();
	private class MailListScrollAction extends UIUtil.ActionInScroll {
		
		public MailListScrollAction() {
			new UIUtil().super();
		}
		
		@Override
		protected void actionClear() {
			MailView.this.mailList.clear();
		}

		@Override
		protected void actionScroll(int totalItemCount) {
			fetchMailList(totalItemCount);
		}

		@Override
		protected void actionRemove(Object o) {
			MailView.this.mailList.remove(o);
		}
	}
	
	private class MailListAdapter extends BaseAdapter {
    	
    	private List<MailObject> list;
    	
		public MailListAdapter(List<MailObject> list) {
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
				tv =  LayoutInflater.from(MailView.this.context)
					.inflate(SettingActivity.displayHd(MailView.this.context)?R.layout.mail_item_hd:R.layout.mail_item, parent, false);
			} else {
				tv =  convertView;
			}
			TextView title = (TextView)tv.findViewById(R.id.mailItemTitle);
			TextView sender = (TextView)tv.findViewById(R.id.mailItemSender);
			TextView date = (TextView)tv.findViewById(R.id.mailItemDate);
			TextView number = (TextView)tv.findViewById(R.id.mailItemNumber);
			
			
			title.setText(list.get(position).getTitle());
			sender.setText(list.get(position).getSender());
			number.setText(list.get(position).getNumber());
			date.setText(list.get(position).getDate());
			if ("+".equals(list.get(position).getStatus()))
				title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mail_forward, 0, 0, 0);
			else if ("r".equals(list.get(position).getStatus()))
				title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mail_replay, 0, 0, 0);
			else if ("m".equals(list.get(position).getStatus()))
				title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mail_send, 0, 0, 0);
			else if ("b".equals(list.get(position).getStatus()))
				title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mail_replyall, 0, 0, 0);
			return tv;
		}
	}
}
