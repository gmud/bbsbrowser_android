package leeon.mobile.BBSBrowser;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import leeon.mobile.BBSBrowser.UIUtil.GroupListener;
import leeon.mobile.BBSBrowser.UIUtil.ImageLoadedCacheFileHandle;
import leeon.mobile.BBSBrowser.UIUtil.PatternListener;
import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.models.DocObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.Browser;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ContentListRender {
	
	private final static Map<String, String> COLOR_MAP = new HashMap<String, String>();
	static {
		COLOR_MAP.put("40", "#ffffff");
		COLOR_MAP.put("41", "#ff0000");
		COLOR_MAP.put("42", "#00ff00");
		COLOR_MAP.put("43", "#ffff00");
		COLOR_MAP.put("44", "#0000ff");
		COLOR_MAP.put("45", "#ff00ff");
		COLOR_MAP.put("46", "#00ffff");
		COLOR_MAP.put("47", "#ffffff");
		
		
		COLOR_MAP.put("030", "#000000");
		COLOR_MAP.put("130", "#000000");
		COLOR_MAP.put("031", "#800000");
		COLOR_MAP.put("131", "#b00000");
		COLOR_MAP.put("032", "#008000");
		COLOR_MAP.put("132", "#00b000");
		COLOR_MAP.put("033", "#808000");
		COLOR_MAP.put("133", "#b0b000");
		COLOR_MAP.put("034", "#000080");
		COLOR_MAP.put("134", "#0000b0");
		COLOR_MAP.put("035", "#800080");
		COLOR_MAP.put("135", "#b000b0");
		COLOR_MAP.put("036", "#008080");
		COLOR_MAP.put("136", "#00b0b0");
		COLOR_MAP.put("037", "#000000");
		COLOR_MAP.put("137", "#000000");
	}
	
	private Handler handler = new Handler();
	
	private Context context;
	private boolean renderColorContent;
	private boolean reRenderImg;
	private boolean qmdRenderImg;
	private boolean renderImg;
	private TextView t;
	private DocObject doc;
	
	public ContentListRender(Context context, boolean renderColorContent, boolean renderImg, boolean reRenderImg, boolean qmdRenderImg, TextView t, DocObject doc) {
		this.context = context;
		this.renderColorContent = renderColorContent;
		this.renderImg = renderImg;
		this.reRenderImg = reRenderImg;
		this.qmdRenderImg = qmdRenderImg;
		this.t = t;
		this.doc = doc;
	}
	
	//执行渲染方法
	public void render(String source) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		htmlContentDeal(source, ssb);
		t.setText(ssb);
		t.setMovementMethod(LongClickLinkMovementMethod.getInstance());
	}
	
	//安装\n分隔成一块一块处理
	//有": "开始，说明该行是回文，分为一块
	//有"--"开始，说明以下是签名档，分为一块
	//否则是另一块
	private void htmlContentDeal(String source, SpannableStringBuilder ssb) {
		//解析用变量
		StringBuffer block = new StringBuffer();
		int inBlock = 0;//0，正文，1，回文，-1，签名档
		int start = 0, end = source.indexOf('\n');
		while (end != -1) {
			String line = source.substring(start, end);
			//--说明下面是qmd的内容了
			//inb = -1,且一直持续到最后
			if (line.equals("--")) {
				//上一行是正文
				if (inBlock == 0) {
					dealBlock(block.toString(), ssb);
					block.setLength(0);
					inBlock = -1;
				} else if (inBlock == 1) {
					dealReBlock(block.toString(), ssb);
					block.setLength(0);
					inBlock = -1;
				}
			//有": "开始，说明该行是回文
			} else if (line.startsWith(": ")) {
				//上一行是回文，加入block
				//上一行是正文，结束block，并处理正文,接下来inB = 1
				if (inBlock == 0) {
					dealBlock(block.toString(), ssb);
					block.setLength(0);
					inBlock = 1;
				}
			//否则是正文
			} else {
				//上一行是正文，加入block
				//上一行是回文，结束block，并处理回文,接下来inB = 0
				if (inBlock == 1) {
					dealReBlock(block.toString(), ssb);
					block.setLength(0);
					inBlock = 0;
				}
			}
			block.append(line).append("\n");
			
			//解析下一行
			start = end+1;
			end = source.indexOf('\n', start);
		}
		
		//处理最后一行
		String line = source.substring(start);
		if (line.equals("--")) {
			if (inBlock == 0) {
				dealBlock(block.toString(), ssb);
				block.setLength(0);
				inBlock = -1;
			} else if (inBlock == 1) {
				dealReBlock(block.toString(), ssb);
				block.setLength(0);
				inBlock = -1;
			}
		} else if (line.startsWith(": ")) {
			if (inBlock == 0) {
				dealBlock(block.toString(), ssb);
				block.setLength(0);
				inBlock = 1;
			}
		} else {
			if (inBlock == 1) {
				dealReBlock(block.toString(), ssb);
				block.setLength(0);
				inBlock = 0;
			}
		}
		
		//处理最后的block
		block.append(line);
		if (inBlock == 0)
			dealBlock(block.toString(), ssb);
		else if (inBlock == 1)
			dealReBlock(block.toString(), ssb);
		else
			dealQmdBlock(block.toString(), ssb);
	}
	
	//处理正文块
	private void dealBlock(String block, SpannableStringBuilder ssb) {
		int s = ssb.length();
		renderColor(block, ssb);
		renderUrl(0, ssb, s);
	}
	
	//处理回文块
	private void dealReBlock(String block, SpannableStringBuilder ssb) {
		block = block.replaceAll(UIUtil.IMG_ANSI_PATTERN, "");
		int s = ssb.length();
		ssb.append(block);
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#008b8b")), s, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		renderUrl(1, ssb, s);
	}
	
	//处理回文块
	private void dealQmdBlock(String block, SpannableStringBuilder ssb) {
		int s = ssb.length();
		renderColor(block, ssb);
		renderUrl(-1, ssb, s);
	}
	
	private void renderColorSpan(Object span, SpannableStringBuilder ssb) {
		if (span != null) {
			int s = ssb.getSpanStart(span);
			if (s != -1) {
				ssb.removeSpan(s);
				ssb.setSpan(span, s, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
	
	private void renderColor(String content, SpannableStringBuilder ssb) {
		UIUtil.replacePattern(ssb, content, UIUtil.IMG_ANSI_PATTERN, new PatternListener() {
			//状态记录
			String hl = "0";
			String fc = "";
			String bc = "";
			BackgroundColorSpan bspan;
			ForegroundColorSpan fspan;
			UnderlineSpan uspan;
					
			public void onPatternMatch(String source, SpannableStringBuilder ssb) {
				if (!renderColorContent) return;
				
				if (!source.endsWith("m")) {
					return;
				}
						
				source = source.substring(4, source.length()-1);
				boolean newSpan = false;
						
						//*[m 结束所有的状态
				if (source.length() == 0) {
					fc = "";bc = "";
					renderColorSpan(bspan, ssb);
					renderColorSpan(fspan, ssb);
					renderColorSpan(uspan, ssb);
					bspan = null;fspan = null;uspan = null;
					return;
				}
						
				//有具体的值，那么对根据具体的值对应结束上次一的状态并开始新状态
				String[] tags = source.split(";");						
				for (String tag : tags) {
					if ("0".equals(tag)) {
						hl = "0";fc = "";bc = "";
						renderColorSpan(bspan, ssb);
						renderColorSpan(fspan, ssb);
						renderColorSpan(uspan, ssb);
						bspan = null;fspan = null;uspan = null;
					} else if ("1".equals(tag) &&  !"1".equals(hl)) {
						newSpan = true;
						hl = "1";
					} else if ("4".equals(tag) && uspan == null) {
						uspan = new UnderlineSpan();
						ssb.setSpan(uspan, ssb.length(), ssb.length(), Spannable.SPAN_MARK_MARK);
					} else if ("5".equals(tag)) {
					} else if ("7".equals(tag)) {
					} else if ("8".equals(tag)) {
					} else {
						if (tag.startsWith("3") && tag.length() == 2 && !tag.equals(fc)) {
							newSpan = true;
							fc = tag;
						} else if (tag.startsWith("4") && tag.length() == 2 && !tag.equals(bc)) {
							bc = tag;
							renderColorSpan(bspan, ssb);
							if (COLOR_MAP.containsKey(bc)) {
								bspan = new BackgroundColorSpan(Color.parseColor(COLOR_MAP.get(bc)));
								ssb.setSpan(bspan, ssb.length(), ssb.length(), Spannable.SPAN_MARK_MARK);
							} else {
								bspan = null;
							}
						}
					}
				}
						
				if (newSpan && !"".equals(fc)) {
					renderColorSpan(fspan, ssb);
					if (COLOR_MAP.containsKey(hl+fc)) {
						fspan = new ForegroundColorSpan(Color.parseColor(COLOR_MAP.get(hl+fc)));
						ssb.setSpan(fspan, ssb.length(), ssb.length(), Spannable.SPAN_MARK_MARK);
					} else {
						fspan = null;
					}
				}
			}
					
			public void onEnd(SpannableStringBuilder ssb) {
				if (!renderColorContent) return;
				fc = "";bc = "";
				renderColorSpan(bspan, ssb);
				renderColorSpan(fspan, ssb);
				renderColorSpan(uspan, ssb);
				bspan = null;fspan = null;uspan = null;
			}
		});
	}
	
	private void renderUrl(final int inBlock, SpannableStringBuilder ssb, final int startMatch) {
		UIUtil.makeSpanFromPattern(ssb, UIUtil.IMG_URL_PATTERN, new GroupListener() {
			public void onPatternMatch(String source, SpannableStringBuilder ssb, int start, int end) {
				//如果小于开始匹配的index，说明这个匹配已经执行过了
				if (start < startMatch) return;
				
				String e = source.toLowerCase();
				boolean isImage = e.endsWith(".jpg") || e.endsWith(".jpeg") || e.endsWith(".png") || e.endsWith(".gif") || e.endsWith(".bmp");
				//正文且图片且是图片模式
				//不是正文但是在一般模式中，且是图片模式
				//inblock == 0正文，inblock == -1 smd，inblock == 1回文
				if ((inBlock == 0 || (inBlock == 1 && reRenderImg) || (inBlock == -1 && qmdRenderImg)) && renderImg && isImage) {
					renderImageSpan(source, ssb, start, end);
				} else {
					ssb.setSpan(new URLSpan(source), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		});
	}
	
	private void renderImageSpan(final String source, final SpannableStringBuilder ssb, final int start, final int end) {
		//先占位
		final ImageSpan ispan = new ImageSpan(context, android.R.drawable.gallery_thumb);
		ssb.setSpan(ispan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		UIUtil.loadImageInThread(zipImageURL(source), context, new ImageLoadedCacheFileHandle() {
			public void imageFileLoadedCallback(final File file, final String mimeType) {
				final Bitmap bitmap = UIUtil.returnBitMap(file, context);
				handler.post(new Runnable() {
					public void run() {
						Spannable s = (Spannable)t.getText();
						if (s.getSpanStart(ispan) != -1) {
							s.removeSpan(ispan);
							s.setSpan(new ImageSpan(context, bitmap), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							s.setSpan(new LongClickableSpan() {
								@Override
								public void onClick(View widget) {
									viewImage(file, source, mimeType);
								}

								@Override
								public void onLongClick(View view) {
									showImageContext(file, source, mimeType);
								}
							}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
				});
			}
		});
	}
	
	//zip image options
	private static final int SOMETIME_ZIP = 2;
	private static final int ALWAYS_ZIP = 3;
	private String zipImageURL(String url) {
		final int imageWidth = context.getResources().getDisplayMetrics().widthPixels;
		int o = SettingActivity.zipImage(context);
		if (o == ALWAYS_ZIP || (o == SOMETIME_ZIP && !UIUtil.isWifi(context))) {
			if (url.startsWith(HttpConfig.BBS_URL1)) {
				url = HttpConfig.BBS_URL3 + 
					url.substring(HttpConfig.BBS_URL1.length()) + "?size=" + imageWidth;
			} else if (url.startsWith(HttpConfig.BBS_URL2)) {
				url = HttpConfig.BBS_URL3 + 
						url.substring(HttpConfig.BBS_URL2.length()) + "?size=" + imageWidth;
			} else if (url.startsWith(HttpConfig.BBS_URL3)) {
				url += "?size=" + imageWidth;
			}
		}
		return url;
	}

	private void showImageContext(final File file, final String source, final String mimeType) {
		String[] item = {"查看大图", "保存图片"};
		int[] itemImage = {R.drawable.thumbnail,  R.drawable.filesave, R.drawable.sinaicon, R.drawable.kaixin_icon};
		UIUtil.createContextItem(context, item, itemImage, new DialogInterface.OnClickListener() {					
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						viewImage(file, source, mimeType);
						break;
					case 1:
						saveImage(file, source, mimeType);
						break;
					default:
						break;
				}
			}
		}).setTitle("图片操作")
		.setIcon(R.drawable.kpaint)
		.create().show();
	}
	
	private void saveImage(final File file, final String source, String mimeType) {
		UIUtil.chooseAndCopyFile(context, file, file.getName() + GIFOpenHelper.getImageFileType(mimeType));
	}
	
	private void viewImage(File file, String source, String mimeType) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(source)).putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName()));
		} else {
			Class<?> target = ImageDetailActivity.class;
			if ("image/gif".equals(mimeType)) {
				target = GIFViewActivity.class;
			}
			Intent intent = new Intent(context, target).putExtra("file", file).putExtra("mime", mimeType);
			context.startActivity(intent);
		}
	}

}
