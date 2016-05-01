package leeon.mobile.BBSBrowser;

import leeon.mobile.BBSBrowser.models.MailObject;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MailDetailActivity extends Activity {
	
	private Button replyMailButton;
	private Button sendMailButton;
	private Button delMailButton;
	private CheckBox backupMailCheckBox;

	private EditText mailContentTextReply;
	private EditText mailTitleTextReply;
	private EditText mailReplyUser;
	private TextView mailContentText;
	
	private MailObject mail;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(SettingActivity.displayHd(this)?R.layout.mail_detail_hd:R.layout.mail_detail);		
		
		replyMailButton = (Button)this.findViewById(R.id.replyMailButton);
		sendMailButton = (Button)this.findViewById(R.id.sendMailButton);
		delMailButton = (Button)this.findViewById(R.id.delMailButton);
		backupMailCheckBox = (CheckBox)this.findViewById(R.id.backupMailCheckBox);
		
		mailReplyUser = (EditText)this.findViewById(R.id.mailReplyUser);
		mailContentTextReply = (EditText)this.findViewById(R.id.mailContentTextReply);
		mailTitleTextReply = (EditText)this.findViewById(R.id.mailTitleTextReply);
		mailContentText = (TextView)this.findViewById(R.id.mailContentText);
		
		mail = (MailObject)this.getIntent().getSerializableExtra("mail");
		if (mail != null) {
			if (this.getIntent().getIntExtra("mailSeq", -1) != -1) {
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(this.getIntent().getIntExtra("mailSeq", -1));
			}
			
			delMailButton.setEnabled(true);
			replyMailButton.setEnabled(true);
			mailReplyUser.setEnabled(false);
			mailReplyUser.setText(mail.getSender());
			mailTitleTextReply.setText((mail.getTitle().startsWith("Re: ")?"":"Re: ") + mail.getTitle());
			fetchMail();
		} else {
			delMailButton.setEnabled(false);
			replyMailButton.setEnabled(false);
			mailReplyUser.setEnabled(true);
			changeReply();
		}
		
		changeReply();
		replyMailButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				changeReply();
			}			
		});
		
		sendMailButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				sendMail();
			}			
		});
		
		delMailButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				delMail();
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
	
	static final String MAIL_ACTION_RE = "reMail";
	static final String MAIL_ACTION_NEW = "newMail";
	static final String MAIL_ACTION_DEL = "delMail";
	private void fetchMail() {
		UIUtil.runActionInThread(MailDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newMailAction().conMail(mail);
				ActionFactory.newInstance().newMailAction().conReMail(mail);
			}
			@Override
			public void actionFinish() {
				mail.setContent(mail.getContent().replaceAll(UIUtil.IMG_ANSI_PATTERN, ""));
				mailContentText.setText(mail.getContent());
				mailContentTextReply.setText("\n\n"+mail.getReContent());
			}
		});
	}
	
	private void sendMail() {
		UIUtil.runActionInThread(MailDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				MailObject newMail = new MailObject(mailReplyUser.getText().toString(), 
						mailTitleTextReply.getText().toString(), mailContentTextReply.getText().toString());
				ActionFactory.newInstance().newMailAction().sendMail(newMail, backupMailCheckBox.isChecked());
			}
			@Override
			public void actionFinish() {
				changeReply();
				Toast.makeText(MailDetailActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
				sendMailSuccess();
			}
		});
	}
	
	private void sendMailSuccess() {
		setResult(RESULT_OK, (new Intent()).setAction(mail==null?MAIL_ACTION_NEW:MAIL_ACTION_RE));
		finish();
	}
	
	
	private void delMail() {
		UIUtil.runActionInThread(MailDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newMailAction().delMail(mail);
			}
			@Override
			public void actionFinish() {
				closeAll();
				Toast.makeText(MailDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
				delMailSuccess();
			}
		});
	}
	
	private void delMailSuccess() {
		setResult(RESULT_OK, (new Intent()).setAction(MAIL_ACTION_DEL));
		finish();
	}
	
	private void changeReply() {
		if ("回复".equals(replyMailButton.getText())) {
			replyMailButton.setText("取消");
			mailContentTextReply.setVisibility(View.VISIBLE);
			mailTitleTextReply.setVisibility(View.VISIBLE);
			mailReplyUser.setVisibility(View.VISIBLE);			
			sendMailButton.setEnabled(true);
			backupMailCheckBox.setEnabled(true);
			if (mail != null) {
				mailContentTextReply.requestFocus();
			}
		} else {
			replyMailButton.setText("回复");
			mailContentTextReply.setVisibility(View.GONE);
			mailTitleTextReply.setVisibility(View.GONE);
			mailReplyUser.setVisibility(View.GONE);
			sendMailButton.setEnabled(false);
			backupMailCheckBox.setEnabled(false);
		}
	}
	
	private void closeAll() {
		if ("取消".equals(replyMailButton.getText())) {
			replyMailButton.setText("回复");
			mailContentTextReply.setVisibility(View.GONE);
			mailTitleTextReply.setVisibility(View.GONE);
			mailReplyUser.setVisibility(View.GONE);
		}
		mailContentText.setText(null);
		replyMailButton.setEnabled(false);
		delMailButton.setEnabled(false);
		sendMailButton.setEnabled(false);
		backupMailCheckBox.setEnabled(false);
	}
}
