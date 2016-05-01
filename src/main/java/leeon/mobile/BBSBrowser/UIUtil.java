package leeon.mobile.BBSBrowser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;

import org.apache.http.client.HttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Browser;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface.OnKeyListener;

public class UIUtil {	
	
	/**
	 * 解析content的正则表达式
	 */
	public static final String IMG_URL_PATTERN = "https://[\\w\\p{Punct}=~]+";
	public static final String IMG_ANSI_PATTERN = ">1b\\[[\\d;]*[a-zA-Z]";
	
	
	/**
	 * 共用show error 对话框
	 * @param context
	 */
	public static void showErrorToast(Context context) {
		Toast
		.makeText(context, "系统或者网络不给力呀,歇着吧!", Toast.LENGTH_LONG)
		.show();
	}
	
	/**
	 * 文件选择对话框
	 * @param context 上下文
	 * @param startPath 初始化的起始路径
	 * @param single 单选还是多选还是选择目录(0,1,2)
	 * @param handle 选择后的处理handle
	 * @param filter 文件过滤器
	 * @return 返回对话框实例
	 */
	public static Dialog fileChooseDialog(Context context, String startPath, FileChooseHandle handle) {
		return fileChooseDialog(context, startPath, 2, handle, new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
	}
	
	public static Dialog fileChooseDialog(Context context, String startPath, boolean single, FileChooseHandle handle, FileFilter filter) {
		return fileChooseDialog(context, startPath, single?0:1, handle, filter);
	}
	
	public static Dialog fileChooseDialog(Context context, String startPath, final int single, final FileChooseHandle handle, final FileFilter filter) {
		//当前对话框中的文件列表
		final List<File> fileList = new ArrayList<File>();
		//已经选择的文件列表
		final List<File> choosedList = new ArrayList<File>();
		//初始化时读取初始路径下的文件信息
		toList(new File(startPath), fileList, filter);
		//构造文件列表的适配器
		final FileListAdapter fileListAdapter = new UIUtil().new FileListAdapter(fileList, choosedList, context);
		
		return new AlertDialog.Builder(context)
			.setTitle("选择文件:" + startPath)
			.setIcon(R.drawable.folder)
			.setSingleChoiceItems(fileListAdapter, 0, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					File clickFile = fileList.get(which);
					if (clickFile == null) return;
					
					//第一个项是返回上级目录，clickFile为上级目录
					if (which == 0 && clickFile.getParentFile() != null) {
						toList(clickFile.getParentFile(), fileList, filter);
						fileListAdapter.notifyDataSetChanged();
						((AlertDialog)dialog).setTitle("选择文件:" + clickFile.getParentFile().getAbsolutePath());
					//如果选中了一个目录，那么读取该目录下的文件
					} else if (clickFile.isDirectory()) {
						toList(clickFile, fileList, filter);
						fileListAdapter.notifyDataSetChanged();
						((AlertDialog)dialog).setTitle("选择文件:" + clickFile.getAbsolutePath());
					//选择了文件，那么做选择的处理
					} else {
						//是否已经选择过，如果选过了就在已选择列表中去掉
						if (isChoosed(clickFile, choosedList)) {
							choosedList.remove(clickFile);
						} else {
							choosedList.add(clickFile);
						}
						//如果是单选，点击子项就返回
						if (single == 0) {
							handle.chooseFileCallback(choosedList);
							dialog.dismiss();
						//多选，那么只是改变选中项的颜色
						} else if (single == 1) {
							fileListAdapter.notifyDataSetChanged();
						}
					}
				}
			})
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (single == 1) handle.chooseFileCallback(choosedList);
					else if (single == 2) {
						choosedList.add(fileList.get(0));
						handle.chooseFileCallback(choosedList);
					}
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			}).create();
	}
	
	/**
	 * 判断是否已经选中
	 */
	private static boolean isChoosed(File f, List<File> choosedList) {
		for (File file : choosedList) {
			if (file.equals(f)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将parent目录下的根据filter过滤出来放入fileList中
	 * @param parent 当前文件夹
	 * @param fileList 传入文件列表
	 * @param filter 文件过滤器
	 */
	private static void toList(File parent, List<File> fileList, FileFilter filter) {
		if (parent == null || fileList == null || parent.listFiles(filter) == null) return;
		fileList.clear();
		//第一项是回到上级目录，所以放上parent file
		fileList.add(parent);
		//依次加入parent目录下的文件
		for (File f : parent.listFiles(filter)) {
			if (!f.isHidden()) fileList.add(f);
		}
		//特殊处理，不满5个文件时为了不让对话框的大小自适应缩小，放上些空项
		if (fileList.size() < 5) {
			for (int i = fileList.size(); i <= 5; i++)
				fileList.add(null);
		}
	}


	/**
	 * 用于在后台运行某个线程时，弹出进度对话框的方法
	 * @param context 上下文
	 * @param action 需要在后台线程中运行的操作接口对应的实例
	 */
	public static void runActionInThread(final Context context, final ActionInThread<?> action) {
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.show();
		dialog.setCancelable(false);
		dialog.setMessage("正在加载，请稍候...");

		final OnKeyListener onKeyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
					dialog.dismiss();
					Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		};
		dialog.setOnKeyListener(onKeyListener);

		Thread t = new Thread(new Runnable() {
			public void run() {
				//用于记录错误的信息
				action.exception = false;
				action.exceptionMsg = null;
				action.bglist = null;
				try {
					action.action();
				} catch (NetworkException e) {
					action.exception = true;
					Log.e("uiutil", "run action error", e);
				} catch (ContentException e) {
					action.exceptionMsg = e.getMessage();
					Log.e("uiutil", "run action error", e);
				} finally {
					//运行结束关闭进度对话框
					dialog.dismiss();
				}
			}
		});
		//进度对话框关闭时执行后续操作，
		//如果发生了错误，弹出错误提示
		//否则执行actionFinish()
		dialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if (action.exception) {
					UIUtil.showErrorToast(context);
					action.actionError();
				} else if (action.exceptionMsg != null) {
					Toast.makeText(context, action.exceptionMsg, Toast.LENGTH_SHORT).show();
					action.actionError();
				} else {
					action.actionFinish();
				}

			}
		});		
		t.start();
	}	
	
	/**
	 * 需要在后台线程中运行的操作接口
	 * @author Administrator
	 */
	public static abstract class ActionInThread<T> {
		boolean exception;		
		String exceptionMsg;
		List<T> bglist;
		
		/**
		 * 需要被执行的操作
		 */
		public abstract void action() throws NetworkException, ContentException;
		
		/**
		 * 执行完成后的动作
		 */
		public abstract void actionFinish();
		
		/**
		 * 执行错误时的动作
		 */
		protected void actionError() {
		}
	}
	
	/**
	 * ListView向下滚动到底时，发出请求获取更多数据的抽象类
	 * 对于每一个ListView做一个该抽象类的实现类，即可实现
	 * 滚动到底，获取更多数据的效果
	 */
	public abstract class ActionInScroll {
		
		//用于锁定用户操作，不能滚动多次，提交多批同样请求的变量
		private Integer preTotalItemCount = 0;
		private Integer preTotalItemCountBackup = 0;
		//相关的listview控件
		private ListView view;
		//判断该此操作是autoscroll的还是refresh刷新的
		protected boolean auto;
		
		//给list view增加 onscroll 监听器的方法
		//在使用前需要被调用
		public void addActionWhenScroll(ListView view) {
			this.view = view;
			view.setOnScrollListener(new OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (totalItemCount <= 0) return;
					//已经滚动到底的判断，到底的话触发获取更多数据的操作，并将目前的total count传入
					//便于计算翻页的信息
					if (firstVisibleItem + visibleItemCount >= totalItemCount) { 
						ActionInScroll.this.onScroll(totalItemCount, true);
					}
				}
				public void onScrollStateChanged(AbsListView view, int scrollState) {}				
			});	
		}
		
		//内部私有的获取更多数据操作的方法
		//将调用供使用者实现的抽象方法
		private void onScroll(int totalItemCount, boolean auto) {
			//锁定preTotalItemCount
			//预先给preTotalItemCount加上页面大小值
			//如果后续触发的action发现totalItemCount小于这个预先值了
			//说明同样的action已经触发过了，或者已经没有数据可获取了，
			synchronized (this.preTotalItemCount) {
				//System.out.println("totalItem:" + totalItemCount + ":" + preTotalItemCount);
				if (totalItemCount < this.preTotalItemCount) return;
				else this.preTotalItemCount += this.pageSize();
				this.auto = auto;//获取改写权后才能改写auto属性
				actionScrollPre(auto);
			}			
			actionScroll(totalItemCount);
		}
		
		//外部调用刷新list view的方法
		//主要需要锁定preTotalItemCOunt的情况下，将其置为0
		//入参用于传入当前的list中已经有的item的数量
		//当currentcount为0时，表示清空整个列表从新刷新，否则表示部分刷新
		public void refresh(int currentTotalItemCount) {
			synchronized (this.preTotalItemCount) {
				if (currentTotalItemCount == 0) {
					view.setSelectionAfterHeaderView();
					actionClear();
				}
				else {
					actionClear(currentTotalItemCount);
				}
				//手工刷新时备份一次count
				this.preTotalItemCountBackup = this.preTotalItemCount;
				this.preTotalItemCount = currentTotalItemCount;
			}
			onScroll(currentTotalItemCount, false);
		}
		
		public void refresh() {
			refresh(0);
		}
		
		//当删除了一个list中的数据时调用的方法
		//在锁定preTotalItemCOunt的情况下，将其--
		public void remove(Object o) {
			if (this.preTotalItemCount <= 0) return;
			synchronized (this.preTotalItemCount) {
				actionRemove(o);
				this.preTotalItemCount--;
			}
		}
		
		//scroll失败的情况下，需要rollback prefTotalItemCount
		public void rollback() {
			if (this.preTotalItemCount <= 0) return;
			synchronized (this.preTotalItemCount) {
				actionRollback(auto);
				//自动刷新，通过减去页数进行rollback
				this.preTotalItemCount -= this.pageSize();
				if (this.preTotalItemCount < 0) this.preTotalItemCount = 0;
				if (!auto) {//手工设置过prototaltiemcount，还原成手工设置前的值
					this.preTotalItemCount = this.preTotalItemCountBackup;
				}
			}
		}
		
		//页面大小设定
		protected int pageSize() {return HttpConfig.BBS_PAGE_SIZE;}
		//预处理action rollback，可以重载也可以不
		protected void actionRollback(boolean auto) {}
		//预处理action scroll，可以重载也可以不
		protected void actionScrollPre(boolean auto) {}
		//用于清空list的action，入参表示list中保留多少item
		protected void actionClear(int currentTotalItemCount) {}
		//必须重载的获取更多数据的action
		protected abstract void actionScroll(int totalItemCount);
		//必须重载用于清空list的action
		protected abstract void actionClear(); 
		//必须重载用于删除list中一个对象的action
		protected abstract void actionRemove(Object o);
	}
	
	/**
	 * 文件选择对话框选择成功后的回调接口
	 */
	public interface FileChooseHandle {
		/**
		 * 回调的方法
		 * @param choosedList 被选中的文件列表
		 */
		public void chooseFileCallback(List<File> choosedList);
	}
	
	/**
	 * 文件选择对话框界面ListView的适配器类
	 */
	private class FileListAdapter extends BaseAdapter {
    	
    	private List<File> list;
    	private List<File> choosed;
    	private Context context;
    	
		public FileListAdapter(List<File> list, List<File> choosed, Context context) {
			this.list = list;
			this.choosed = choosed;
			this.context = context;
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
				tv = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
			} else {
				tv = convertView;
			}
			TextView l = (TextView)tv.findViewById(android.R.id.text1);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			l.setCompoundDrawablePadding(5);
			
			//第一个位置是回到上级目录
			if (position == 0) {
				l.setText(". .");
				l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.redo, 0, 0, 0);
				l.setTextColor(Color.BLACK);
			//为空的显示空字符串
			} else if (list.get(position) == null) {
				l.setText("");
				l.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				l.setTextColor(Color.BLACK);
			} else {
				l.setText(list.get(position).getName());
				//目录黑色、文件蓝色
				if (list.get(position).isDirectory()) {
					l.setTextColor(Color.BLACK);
					l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder, 0, 0, 0);
				} else {
					l.setTextColor(Color.BLUE);
					l.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kpaint, 0, 0, 0);
				}
				//选中的红色
				if (isChoosed(list.get(position), choosed)) {
					l.setTextColor(Color.RED);
				}
			}
			
			return tv;
		}
	}
	
	/**
	 * 根据uri获取照片的局对路径
	 * @param uri 照片uri
	 * @param context 上下文
	 * @return 返回绝对路径
	 */
	public static String getAbsoluteImagePath(Uri uri, Activity context) {
		String [] proj={MediaStore.Images.Media.DATA};
		Cursor cursor = context.managedQuery(uri,
					proj,// Which columns to return
					null,// WHERE clause; which rows to return (all rows)
					null,// WHERE clause selection arguments (none)
					null);// Order-by clause (ascending by name)
		int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(columnIndex);
	}
	
	/**
	 * 清理缓存
	 * day表示离现在有多少天
	 */
	public static void clearCache(Context context, int day) {
		final long now = System.currentTimeMillis();
		final long d = day*24*3600*1000;
		if (context.getCacheDir().exists()) {
			deleteFile(context.getCacheDir(), new FileFilter() {
				public boolean accept(File file) {
					if (file.isDirectory()) return false;
					if (now - file.lastModified() > d) return true;
					return false;
				}
			}, false);
		}
	}
	
	//用于记录正在下载图片的线程
	private static Map<String, Thread> threadpool = new Hashtable<String, Thread>();
	/**
	 * 在线程中load图片
	 * @param url load的url
	 * @param useCache 使用cache
	 * @param context 上下文
	 */
	private static void loadImageInThread(final String url, final boolean useCache, final Context context,
			final ImageLoadedCacheFileHandle ch, final ImageLoadedByteArrayHandle bh, final HttpClient client) {
		//如果使用cache，那么创建cache目录
		if (useCache && !context.getCacheDir().exists()) context.getCacheDir().mkdir();

		final Thread t = new Thread() {
			public void run() {
				try {
					//如果出现多个相同url请求进行阻塞
					Thread t1 = null;
					synchronized(threadpool) {
						//先判断当前有没有正在执行该url的线程，
						//如果有，将当前的替换成现在的，现在加入到当前之后
						//之后再出现的请求该url的加入到现在之后
						//如果没有，就直接将url加入正在执行的队列中
						if (threadpool.containsKey(url)) {
							t1 = threadpool.get(url);
						}
						threadpool.put(url, this);
					}
					try {
						 if (t1 != null) t1.join();
					} catch (InterruptedException e) {
						Log.e("uiutil", "load image error", e);
					}
					
					//使用cache
					if (useCache) {
						File cache = cacheFromURL(url, context);
						//且cache不存在，从网络下载
						if (!cache.exists()) {
							HTTPUtil.downloadFile(url, cache, client);
						}
						//防止加载过程很多线程并发，造成内存回收来不及，使用contex来锁定
						//非cache模式，因为有http client 并发数限制，所以可以不加锁
						synchronized(context) {
							ch.imageFileLoadedCallback(cache, GIFOpenHelper.readImageType(cache));
						}
					} else {
						byte[] bs = HTTPUtil.downloadFileToByteArray(url, client);
						bh.imageFileLoadedCallback(bs, GIFOpenHelper.getImageType(bs));
					}
				} catch (NetworkException e) {
					Log.e("uiutil", "load image error", e);
				} finally {
					//该线程执行完成，看看当前是否在线程池里
					//如果在，说明没有其他join自己，删除掉
					//如果不在，说明自己被join了，不需要删除
					synchronized(threadpool) {
						if (threadpool.containsValue(this))
							threadpool.remove(url);
					}
				}
			}
		};
		t.start();
	}
	
	//缓存图片的加载方式
	public static void loadImageInThread(String url, Context context, ImageLoadedCacheFileHandle ch) {
		loadImageInThread(url, true, context, ch, null, null);
	}
	
	public static void loadImageInThread(String url, Context context, ImageLoadedCacheFileHandle ch, HttpClient client) {
		loadImageInThread(url, true, context, ch, null, client);
	}
	
	public static void loadImageInThread(String url, Context context, final Handler handler, final ImageView i) {
		loadImageInThread(url, context, handler, i, null);
	}
	
	public static void loadImageInThread(String url, Context context, final Handler handler, final ImageView i, HttpClient client) {
		loadImageInThread(url, context, 
			new ImageLoadedCacheFileHandle() {
				public void imageFileLoadedCallback(final File file, String type) {
					handler.post(new Runnable() {
						public void run() {
							i.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
						}
					});
				}
			}, client);
	}
	
	//非缓存图片的加载方式
	public static void loadImageInThread(String url, Context context, ImageLoadedByteArrayHandle bh) {
		loadImageInThread(url, false, context, null, bh, null);
	}
	
	public static void loadImageInThread(String url, Context context, ImageLoadedByteArrayHandle bh, HttpClient client) {
		loadImageInThread(url, false, context, null, bh, client);
	}
	
	private static final String[] crc32_prefix_array = {"00000000", "0000000", "000000", "00000", "0000", "000", "00", "0", ""};
	private static File cacheFromURLCore(String url, Context context) {
		CRC32 crc32 = new CRC32();
		crc32.update(url.getBytes());
		String ret = Long.toHexString(crc32.getValue());
		return new File(context.getCacheDir().getPath() + "/" + crc32_prefix_array[ret.length()] + ret);
		//return new File(context.getCacheDir().getPath() + "/" + url.replace('/', '_').replace(':', '_'));
	}
	
	//用于将多个镜像网站的图片映射到一个
	private static final Map<String, String> URL_MIRROR = new HashMap<String, String>(){
		private static final long serialVersionUID = 4489764689139785609L; {
		put(HttpConfig.BBS_URL2, HttpConfig.BBS_URL1);
		put(HttpConfig.BBS_URL3, HttpConfig.BBS_URL1);
	}};
	
	public static File cacheFromURL(String url, Context context) {
		return cacheFromURL(url, context, URL_MIRROR);
	}
	
	public static File cacheFromURL(String url, Context context, Map<String, String> map) {
		for (String u : map.keySet()) {
			if (url.startsWith(u)) {
				url = map.get(u) + url.substring(u.length());
				return cacheFromURLCore(url, context);
			}
		}
		return cacheFromURLCore(url, context);
	}
	
	
	/**
	 * 图片下载成功后的回调接口
	 */
	public interface ImageLoadedCacheFileHandle {
		/**
		 * 使用cache时的回调的方法
		 * @param 
		 */
		public void imageFileLoadedCallback(File file, String type);	
	}
	
	/**
	 * 图片下载成功后的回调接口
	 */
	public interface ImageLoadedByteArrayHandle {
		/**
		 * 使用非cache时的回调的方法
		 * @param 
		 */
		public void imageFileLoadedCallback(byte[] bs, String type);
	}
	
	
	/**
	 * 渲染span时的正则表达式替换方法
	 */
	public static void replacePattern(SpannableStringBuilder ssb, String source, String pattern, PatternListener lsr) {
		if (pattern == null || pattern.length() == 0 || lsr == null)
			return;

		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(source);
		int s = 0;
		while (m.find()) {
			ssb.append(source.subSequence(s, m.start()));			
			lsr.onPatternMatch(m.group(), ssb);
			s = m.end();
		}
		ssb.append(source.substring(s));
		lsr.onEnd(ssb);
	}

	public interface PatternListener {
		public void onPatternMatch(String source, SpannableStringBuilder ssb);
		
		public void onEnd(SpannableStringBuilder ssb);
	}

	/**
	 * 渲染span时的正则表达式替换方法
	 */
	public static void makeSpanFromPattern(SpannableStringBuilder ssb, String pattern, GroupListener lsr) {
		if (pattern == null || pattern.length() == 0 || lsr == null)
			return;
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(ssb);
		while (m.find()) {
			String g = m.group();
			lsr.onPatternMatch(g, ssb, m.start(), m.end());
		}
	}

	public interface GroupListener {
		public void onPatternMatch(String source, SpannableStringBuilder ssb,
				int start, int end);
	}
	
	/**
	 * 渲染html时构造图片的方法
	 * @param cache
	 * @return
	 */
	public static Bitmap returnBitMap(File cache, Context context) {
		//图片大小
		final int imageWidth = context.getResources().getDisplayMetrics().widthPixels - 20;
		if (!cache.exists()) return null;
		
		BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(cache.getAbsolutePath(), o);
        
        if (o.outWidth == -1) return null;
        
        int scale = 1;
        if (o.outWidth > imageWidth) {
            scale = o.outWidth/imageWidth;
        }

        BitmapFactory.Options o1 = new BitmapFactory.Options();
        o1.inSampleSize = scale;
        
		Bitmap bitmap = BitmapFactory.decodeFile(cache.getAbsolutePath(), o1);
		if (bitmap == null) {
			return null;
		}
		
		if (bitmap.getWidth() > imageWidth)
			try {
				return Bitmap.createScaledBitmap(bitmap, imageWidth, imageWidth*bitmap.getHeight()/bitmap.getWidth(), true);
			} finally {
				if (!bitmap.isRecycled()) bitmap.recycle();
			}
		else 
			return bitmap;
	}
	
	/**
	 * 使用html.fromhtml时用于渲染连接
	 */
	public static void renderBaseURL(Spannable content, String baseUrl, final boolean underline, final int color) {
		URLSpan[] us = content.getSpans(0, content.length(), URLSpan.class);
		for (URLSpan u : us) {
			String url = u.getURL();
			if (url != null && url.startsWith("/")) {
				int s = content.getSpanStart(u);
				int e = content.getSpanEnd(u);
				int f = content.getSpanFlags(u);
				
				content.removeSpan(u);
				content.setSpan(new URLSpan(baseUrl + url) {
					@Override
				    public void updateDrawState(TextPaint ds) {
				        super.updateDrawState(ds);
				        ds.setUnderlineText(underline);
				        if (color != 0) ds.setColor(color);
				    }
				}, s, e, f);
			}
		}
	}
	
	/**
	 * 使用html.fromhtml时用于渲染图片
	 */
	public static void renderImage(Spannable content, String baseUrl, final Handler handler, final TextView t) {
		ImageSpan[] is = content.getSpans(0, content.length(), ImageSpan.class);
		for (final ImageSpan i : is) {
			String url = i.getSource();
			if (url != null) {
				final String src = url;
				
				UIUtil.loadImageInThread(src, t.getContext(), 
					new ImageLoadedCacheFileHandle() {
						public void imageFileLoadedCallback(final File file, String type) {
							final Bitmap bitmap = returnBitMap(file, t.getContext());
							handler.post(new Runnable() {
								public void run() {
									Spannable sp = (Spannable)t.getText();
									int s = sp.getSpanStart(i);
									int e = sp.getSpanEnd(i);
									sp.removeSpan(i);
									sp.setSpan(new ImageSpan(bitmap), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								}
							});
						}
				});		
			}
		}
	}
	
	public static void viewBigImage(final String src, ImageView i, final Context context) {
		viewBigImage(src, i, context, null);
	}
	
	public static void viewBigImage(final String src, ImageView i, final Context context, final HttpClient client) {
		if (src != null && src.length() != 0) {
			i.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(src))
							.putExtra(Browser.EXTRA_APPLICATION_ID, ((Activity)context).getPackageName()));
					} else {
						viewImage(src, context, client);
					}
				}
			});
		}
	}
	
	private static void viewImage(final String src, final Context context, final HttpClient client) {
		runActionInThread(context, new UIUtil.ActionInThread<Object>() {
			File cache = cacheFromURL(src, context);
			@Override
			public void action() throws NetworkException, ContentException {
				if (!cache.exists()) {
					HTTPUtil.downloadFile(src, cache, client);
				}
			}

			@Override
			public void actionFinish() {
				if (cache.exists()) {
					Class<?> target = ImageDetailActivity.class;
					String mimeType = GIFOpenHelper.readImageType(cache);
					if ("image/gif".equals(mimeType)) {
						target = GIFViewActivity.class;
					}
					Intent intent = new Intent(context, target).putExtra("file", cache).putExtra("mime", mimeType);
					context.startActivity(intent);
				}
			}
			
		});	
	}
	
	/**
	 * 用于自己定义弹出式菜单
	 * 该菜单可以加入图标
	 */
	public static AlertDialog.Builder createContextItem(
		Context context, String[] itemText, int[] itemImage,
		DialogInterface.OnClickListener lsr) {
			return new AlertDialog.Builder(context)
			.setAdapter(new UIUtil().new AttListAdapter(itemText, itemImage, context), lsr);
	}
	
	public static AlertDialog.Builder createContextItem(
		Context context, List<String> itemText, List<Integer> itemImage,
		DialogInterface.OnClickListener lsr) {
			int[] item = null;
			if (itemImage != null) {
				item = new int[itemText.size()];
				for (int i = 0; i < item.length && i < itemText.size(); i ++) {
					item[i] = itemImage.get(i);
				}
			}
			return createContextItem(context, itemText.toArray(new String[0]), item, lsr);
	}
	
	public interface ContextItemActionListener {
		public void onAction(DialogInterface dialog);
	}
	
	private class AttListAdapter extends BaseAdapter {
		private String[] item;
		private int[] itemImage;
		private Context context;
		
		public AttListAdapter(String[] item, int[] itemImage, Context context) {
			this.item = item;
			this.itemImage = itemImage;
			this.context = context;
		}
		
		public int getCount() {
			return item.length;
		}

		public Object getItem(int position) {
			return item[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
			} else {
				tv = convertView;
			}
			TextView l = (TextView)tv.findViewById(android.R.id.text1);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			l.setCompoundDrawablePadding(5);
			l.setText(item[position]);
			l.setTextColor(Color.BLACK);
			if (itemImage != null && position < itemImage.length && itemImage[position] != -1)
			l.setCompoundDrawablesWithIntrinsicBounds(itemImage[position], 0, 0, 0);
			
			return tv;
		}
	}
	
	
	/**
	 * 弹出对话框选择目录并拷贝文件
	 * @param file 源文件
	 * @param defaultFileName 拷贝到新的文件的文件名，null的话保留原文件名
	 */
	public static void chooseAndCopyFile(final Context context, final File file, final String defaultFileName) {
		UIUtil.fileChooseDialog(context, Environment.getExternalStorageDirectory().getAbsolutePath(), new FileChooseHandle(){
			public void chooseFileCallback(List<File> choosedList) {
				if (choosedList != null && choosedList.size() != 0) {
					File to = choosedList.get(0);
					if (to != null && to.isDirectory()) {
						to = new File(to.getAbsolutePath()+"/"+(defaultFileName==null?file.getName():defaultFileName));
						copyFile(file, to);
						
						//如果是图片，更新媒体库
						Uri data = Uri.parse("file://" + to.getAbsolutePath());   
				        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
					}
				}
			}
		}).show();
	}
	
	/**
	 * 文件拷贝
	 * @param source 源文件
	 * @param target 目标文件
	 */
	public static void copyFile(File source, File target) {
		byte[] bf = new byte[1024];
		int l = 0;
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(target);
			while ((l = in.read(bf)) != -1) {
				out.write(bf, 0, l);
			}
			out.flush();
		} catch (Exception e) {
			Log.e("uiutil", "copy file error", e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				Log.e("uiutil", "copy file error", e);
			}
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				Log.e("uiutil", "copy file error", e);
			}
		}
	}
	
	/**
	 * 删除指定目录下的文件
	 * 如果filter为空，deleteDirOnEmpty 为 true， 那么意味着删除整个dir
	 * 如果filter为空，deleteDirOnEmpty 为 false， 那么意味着删除整个dir下所有文件，但目录结构保留
	 * 如果filter包含了目录及满足条件的文件，那么删除目录机子目录的所有满足该条件的文件，deleteDirOnEmpty会影响涉及子目录
	 * 如果filter不包含了目录只包含满足条件的文件，那么删除当前目录所有满足该条件的文件，deleteDirOnEmpty只会影响当前目录
	 * deleteDirOnEmpty是否会起作用和作用的目录是否为空有关系
	 * @param dir 目录
	 * @param filter 过滤器，用于过滤需要删除的文件
	 * @param deleteDirOnEmpty 最后是否要删除目录本身
	 */
	public static void deleteFile(File dir, FileFilter filter, boolean deleteDirOnEmpty) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) return;

		File[] fs = dir.listFiles(filter);
		for (File f : fs) {
			if (f.exists()) {
				if (f.isDirectory()) {
					deleteFile(f, filter, deleteDirOnEmpty);
				} else {
					f.delete();
				}
			}
		}
		
		if (dir.list().length == 0 && deleteDirOnEmpty) {
			dir.delete();
		}
	}
	
	/**
	 * 创建缓存文件
	 * @param fileName
	 * @param content
	 */
	public static void createCacheFile(String fileName, String content, Context context) {
		if (!context.getCacheDir().exists()) context.getCacheDir().mkdir();
		if (fileName == null || content == null) return;
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(context.getCacheDir().toString() + "/" + fileName));
			out.write(content.getBytes());
			out.flush();
		} catch (Exception e) {
			Log.e("uiutil", "create file error", e);
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				Log.e("uiutil", "create file error", e);
			}
		}
	}
	
	/**
	 * 删除缓存文件
	 * @param fileName
	 */
	public static void deleteCacheFile(String fileName, Context context) {
		File file = new File(context.getCacheDir().toString() + "/" + fileName);
		if (file.exists()) {
			file.delete();
		}
	}
	
	public static byte[] readCacheFile(String fileName, Context context) {
		File file = new File(context.getCacheDir().toString() + "/" + fileName);
		if (!file.exists()) return null;
		
		FileInputStream in = null;
		byte[] ret = null;
		try {
			in = new FileInputStream(new File(context.getCacheDir().toString() + "/" + fileName));
			ret = new byte[in.available()];
			in.read(ret);
			return ret;
		} catch (Exception e) {
			Log.e("uiutil", "create file error", e);
			return ret;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				Log.e("uiutil", "read file error", e);
			}
		}
	}
	
	/**
	 * 判断是否是wifi网络
	 */
	public static boolean isWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}  

}
