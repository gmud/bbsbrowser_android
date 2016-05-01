package leeon.mobile.BBSBrowser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leeon.mobile.BBSBrowser.models.MailObject;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 检查新邮件的后台线程
 */
public class NewMailThread extends Thread {
	
	private Context context;
	private NotificationManager nm;
	
	//用于记录新邮件的唯一标示ID的map, Key为邮件的bbs中的ID,value为本系统中的整型ID
	private Map<String, Integer> notifiedMap = new HashMap<String, Integer>();
	//用于记录新邮件的唯一标示ID
	private Integer notifySeq = 0;
	
	public NewMailThread(Context context) {
		this.context = context;
	}
	
	//线程运行的run方法
	public void run() {
		nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (SettingActivity.checkMail(context)) {
			CHECK_DELAY = SettingActivity.checkMailInterval(context);
			handler.sendEmptyMessage(CHECK_MAIL);
		}
	}
	
	//调用该方法，停止定时检查操作
	public void stopCheck() {
		nm.cancelAll();
		notifiedMap.clear();
		notifySeq = 0;
		handler.removeMessages(CHECK_MAIL);
	}
	//定期检查的标志位
	private static final int CHECK_MAIL = 1;
	//定时周期，目前5分钟
	private static int CHECK_DELAY = 5*60*1000;
	//定期检查操作的handel
	private final Handler handler = new Handler() {
		@Override 
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CHECK_MAIL: {
					//判断登录
					if (UserUtil.CURRENT_USER_ID != null) {
						try {
							//是否有新邮件
							List<MailObject> list = ActionFactory.newInstance().newMailAction().newMailList();
							if (list != null && list.size() != 0) {
								for (MailObject mail : list) {
									//该邮件是否已经发出了提示，没有的话记录该邮件，并给出提示
									if (!notifiedMap.containsKey(mail.getId())) {
										notifiedMap.put(mail.getId(), notifySeq);
										showNotification(mail, notifySeq++);
									}
								}
							}
						} catch (Exception e) {
							Log.e("mail thread", "check mail error", e);
						}
					}
					//过check_delay周期后，继续发出检查的标志位
					sendMessageDelayed(obtainMessage(CHECK_MAIL), CHECK_DELAY);
				}break;
				default:
					super.handleMessage(msg);
			}
		}
	};
	
	//发出一个有新邮件到达的notification
	public void showNotification(MailObject mail, Integer mailSeq) {
		String title = "来自日月光华BBS的邮件: From " + mail.getSender(); 
		Notification notification = new Notification(R.drawable.mail_forward, title, System.currentTimeMillis());

		Intent intent = new Intent(context, MailDetailActivity.class);
		intent.putExtra("mail", mail);
		intent.putExtra("mailSeq", mailSeq);
		PendingIntent contentIntent = PendingIntent.getActivity(context, mailSeq, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.setLatestEventInfo(context, title, mail.getTitle(), contentIntent);
		nm.notify(mailSeq, notification);
    }
}
