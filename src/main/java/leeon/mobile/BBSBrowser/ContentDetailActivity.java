package leeon.mobile.BBSBrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ContentDetailActivity extends Activity {
	
	private Button postButton;
	private Button delButton;
	private Button attButton;
	private CheckBox anonyCheckBox;

	private EditText postTitle;
	private EditText postContent;
	private TextView postLabe;
	private RadioGroup sigGroup;
	
	private DocObject doc;
	private DocObject newDoc;
	private BoardObject board;
	private boolean edit = false;
	private int sig = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(SettingActivity.displayHd(this)?R.layout.content_detail_hd:R.layout.content_detail);		
		
		postButton = (Button)this.findViewById(R.id.postButton);
		delButton = (Button)this.findViewById(R.id.delButton);
		attButton = (Button)this.findViewById(R.id.attButton);
		anonyCheckBox = (CheckBox)this.findViewById(R.id.anonyCheckBox);
		
		postTitle = (EditText)this.findViewById(R.id.postTitle);
		postContent = (EditText)this.findViewById(R.id.postContent);
		postLabe = (TextView)this.findViewById(R.id.postLabel);
		sigGroup = (RadioGroup)this.findViewById(R.id.postSig);
		
		doc = (DocObject)this.getIntent().getSerializableExtra("doc");
		board = (BoardObject)this.getIntent().getSerializableExtra("board");
		edit = this.getIntent().getBooleanExtra("edit", false);
		
		if ((board != null && board.isAnony()) || (doc != null && doc.getBoard().isAnony())) {
			anonyCheckBox.setEnabled(true);
			anonyCheckBox.setChecked(true);
		} else {
			anonyCheckBox.setEnabled(false);
		}
		
		UIUtil.runActionInThread(ContentDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (doc != null) {
					newDoc = ActionFactory.newInstance().newPostAction().inPostDoc(doc.getBoard(), doc);
				} else {
					newDoc = ActionFactory.newInstance().newPostAction().inPostDoc(board);
					if (newDoc == null) newDoc = new DocObject("", "", board);
				}
			}
			@Override
			public void actionFinish() {
				if (doc != null) {
					postLabe.setText("回复:" + doc.getTitle());
					edit = edit && (doc.getAuthor().startsWith(UserUtil.CURRENT_USER_ID));
					
					if (!edit) {
						postTitle.setText(newDoc.getTitle());
						postContent.setText("\n" + newDoc.getContent());
						
						delButton.setEnabled(false);
					} else {
						postTitle.setText(doc.getTitle());
						postContent.setText(doc.getContent());
						
						postTitle.setEnabled(false);
						delButton.setEnabled(true);
					}
					postContent.requestFocus();
				} else {
					postLabe.setText("新作");
					delButton.setEnabled(false);
				}
			}
		});
		
		postButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				postAction();
			}			
		});
		
		delButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				deleteAction();
			}			
		});
		
		attButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ATT);
			}
		});
		
		sigGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == View.NO_ID) sig = 1;
				else {
					RadioButton r = (RadioButton)ContentDetailActivity.this.findViewById(checkedId);
					sig = Integer.parseInt(r.getText().toString().substring(2));
				}
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

	private static final int SELECT_IMG = 0;
	private static final int CAP_IMG = 1;
	private static final int DRAW_IMG = 2;
	private static final int DIALOG_ATT = 0;
	private static final String[] DIALOG_ATT_ITEM = {"从媒体库选择", "立刻拍照", "涂鸦", "从文件系统选择"};
	private static final int[] DIALOG_ATT_ITEM_IMAGE = 
		{R.drawable.thumbnail, R.drawable.ksnapshot, R.drawable.krita, R.drawable.folder};
	static final File tmp = new File(Environment.getExternalStorageDirectory() + "/tmp.jpg");
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			List<File> list = new ArrayList<File>();
			if (requestCode == SELECT_IMG) {
				list.add(new File(UIUtil.getAbsoluteImagePath(data.getData(), this)));
			} else if (requestCode == CAP_IMG) {
				list.add(tmp);
			} else if (requestCode == DRAW_IMG) {
				list.add(tmp);
			}
			uploadAction(list);
		}
		
	}
    
    @Override
    protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ATT:
			return UIUtil.createContextItem(this, DIALOG_ATT_ITEM, DIALOG_ATT_ITEM_IMAGE, 
				new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case SELECT_IMG:
								imgAction();
								break;
							case CAP_IMG:
								capAction();
								break;
							case DRAW_IMG:
								drawAction();
								break;
							default:
								batAction();
						}
				}
			}).setTitle("选择图片来源")
			.setIcon(R.drawable.kpaint)
			.create();
		default:
			return null;
		}
    }	
	
    static final String POST_ACTION_NEW = "newPost";
    static final String POST_ACTION_EDIT = "newPost";
    static final String POST_ACTION_RE = "rePost";
    static final String POST_ACTION_DEL = "delPost";
	private void postAction() {
		UIUtil.runActionInThread(ContentDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (doc != null && edit) {
					doc.setContent(postContent.getText().toString());
					ActionFactory.newInstance().newPostAction().sendPostDoc(null, doc, false, true, sig);
				} else {
					newDoc.setTitle(postTitle.getText().toString());
					newDoc.setContent(postContent.getText().toString());
					ActionFactory.newInstance().newPostAction().sendPostDoc(newDoc, doc, (anonyCheckBox.isEnabled()&&anonyCheckBox.isChecked()), edit, sig);
				}
			}
			@Override
			public void actionFinish() {
				postTitle.setEnabled(false);
				postContent.setEnabled(false);
				postButton.setEnabled(false);
				attButton.setEnabled(false);
				Toast.makeText(ContentDetailActivity.this, "发表成功", Toast.LENGTH_SHORT).show();
				postSuccess();
			}
		});
	}
	
	private void postSuccess() {
		if (doc != null) {
			if (!edit)
				setResult(RESULT_OK, (new Intent()).setAction(POST_ACTION_RE).putExtra("doc", newDoc));
			else
				setResult(RESULT_OK, (new Intent()).setAction(POST_ACTION_EDIT).putExtra("doc", doc));
		} else {
			setResult(RESULT_OK, (new Intent()).setAction(POST_ACTION_NEW).putExtra("doc", newDoc));
		}
		finish();
	}
	
	private void deleteAction() {
		UIUtil.runActionInThread(ContentDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				if (doc != null && edit) {
					ActionFactory.newInstance().newPostAction().delPostDoc(doc);
				}
			}
			@Override
			public void actionFinish() {
				postTitle.setEnabled(false);
				postContent.setEnabled(false);
				postButton.setEnabled(false);
				attButton.setEnabled(false);
				delButton.setEnabled(false);
				Toast.makeText(ContentDetailActivity.this, "删帖成功", Toast.LENGTH_SHORT).show();
				deleteSuccess();
			}
		});
	}
	
	private void deleteSuccess() {
		if (doc != null && edit) {
			setResult(RESULT_OK, (new Intent()).setAction(POST_ACTION_DEL).putExtra("doc", doc));
		}
		finish();
	}
	
	private void uploadAction (final List<File> choosedList) {
		UIUtil.runActionInThread(ContentDetailActivity.this, new UIUtil.ActionInThread<Object>() {
			private String msg = "";
			@Override
			public void action() throws NetworkException, ContentException {
				BoardObject b = (board!=null?board:doc.getBoard());
				for (File f : choosedList) {
					if (f.exists() && !f.isDirectory()) {
						String m = ActionFactory.newInstance().newPostAction().sendAttFile(
							f, b.isAttach()?b:HttpConfig.DEFAULT_UPLOAD_BOARD, GIFOpenHelper.getImageFileMimeType(f));
						msg += m + "\n\n";
					}
				}
			}
			@Override
			public void actionFinish() {
				Toast.makeText(ContentDetailActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
				postContent.setText("\n" + msg + postContent.getText());
				if (tmp.exists()) tmp.delete();
			}
		});
	}

	private void batAction() {
		UIUtil.fileChooseDialog(ContentDetailActivity.this, 
			Environment.getExternalStorageDirectory().getAbsolutePath(), false, 
			new UIUtil.FileChooseHandle() {
				public void chooseFileCallback(final List<File> choosedList) {
					uploadAction(choosedList);
				}			
			},
			new FileFilter() {
				public boolean accept(File file) {
					return (file.isDirectory() || 
						file.getName().toLowerCase().endsWith(".jpg") ||
						file.getName().toLowerCase().endsWith(".png") ||
						file.getName().toLowerCase().endsWith(".gif") ||
						file.getName().toLowerCase().endsWith(".jpeg")
					);
				}
				
			}
		).show();
	}
	
	private void imgAction() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select Pic"), SELECT_IMG);
	}
	
	private void capAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, 
			Uri.fromFile(tmp));
		startActivityForResult(intent, CAP_IMG);
	}
	
	private void drawAction() {
		Intent intent = new Intent(this, ContentDrawingActivity.class);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmp));
		startActivityForResult(intent, DRAW_IMG);
	}
	
}
