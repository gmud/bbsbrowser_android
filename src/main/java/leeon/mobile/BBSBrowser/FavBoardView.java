package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class FavBoardView extends BaseGestureView {
	
	private Context context;
	
	private ListView favouriteBoardList;
	private List<BoardObject> boardlist = new ArrayList<BoardObject>();
	
	private String guestUserId;
	private int actionParam = ActionFactory.DEFAULT_PARAM;
	
	public FavBoardView(Context context) {
		super(context);
		this.context = context;
	}
	
	public void refresh() {
		//非fdu的刷新
		if (actionParam != ActionFactory.DEFAULT_PARAM) {
			UIUtil.runActionInThread(context, new UIUtil.ActionInThread<BoardObject>() {
				@Override
				public void action() throws NetworkException, ContentException {
					bglist = UserUtil.getOtherFavList(context, actionParam);
				}
				@Override
				public void actionFinish() {
					if (bglist == null) return;
					if (bglist.isEmpty()) {
						if (actionParam == ActionFactory.SJTU_PARAM)
							bglist.add(new BoardObject("PPPerson", "PPPerson", "美丽人物"));
						else if (actionParam == ActionFactory.YANXI_PARAM)
							bglist.add(new BoardObject("405", "Sex", "人之初"));
					}
					
					boardlist.clear();
					boardlist.addAll(bglist);
					Collections.sort(boardlist, new Comparator<BoardObject>(){
						public int compare(BoardObject object1, BoardObject object2) {
							return object1.getName().compareTo(object2.getName());
						}
					});
					
					((BaseAdapter)favouriteBoardList.getAdapter()).notifyDataSetChanged();
				}
			});
			
			return;
		}
		//fdu的刷新
		if (UserUtil.CURRENT_USER_ID != null || (guestUserId != null && guestUserId.length() != 0)) {		
			UIUtil.runActionInThread(context, new UIUtil.ActionInThread<BoardObject>() {
				@Override
				public void action() throws NetworkException, ContentException {
					if (UserUtil.CURRENT_USER_ID != null) {
						bglist = ActionFactory.newInstance().newLogAction().favBoard();
						UserUtil.saveFavList(context, bglist);
					} else if (guestUserId != null && guestUserId.length() != 0)
						bglist = UserUtil.getFavList(context, guestUserId);
				}
				@Override
				public void actionFinish() {
					if (bglist == null) return;
					boardlist.clear();
					boardlist.addAll(bglist);
					Collections.sort(boardlist, new Comparator<BoardObject>(){
						public int compare(BoardObject object1, BoardObject object2) {
							return object1.getName().compareTo(object2.getName());
						}
					});
					
					((BaseAdapter)favouriteBoardList.getAdapter()).notifyDataSetChanged();
				}
			});
		}
	}
	
	private void delFavBoard(final BoardObject board) {
		if (UserUtil.CURRENT_USER_ID == null) return;
		UIUtil.runActionInThread(context, new UIUtil.ActionInThread<BoardObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				List<BoardObject> list = new ArrayList<BoardObject>();
				list.addAll(boardlist);
				list.remove(board);
				ActionFactory.newInstance().newLogAction().setFavBoard(list);
			}
			@Override
			public void actionFinish() {
				boardlist.remove(board);
				((BaseAdapter)favouriteBoardList.getAdapter()).notifyDataSetChanged();
			}
		});
	}
	
	private void delOtherFavBoard(final BoardObject board) {
		UIUtil.runActionInThread(context, new UIUtil.ActionInThread<BoardObject>() {
			@Override
			public void action() throws NetworkException, ContentException {
				boardlist.remove(board);
				UserUtil.saveOtherFavList(context, boardlist, actionParam);
			}
			@Override
			public void actionFinish() {
				((BaseAdapter)favouriteBoardList.getAdapter()).notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 返回收藏版面
	 */
	public View createFavouriteBoardView(String guestUserId) {
		return createFavouriteBoardView(guestUserId, ActionFactory.DEFAULT_PARAM);
	}
	
	public View createFavouriteBoardView(int actionParam) {
		return createFavouriteBoardView(null, actionParam);
	}
	
	public View createFavouriteBoardView(String guestUserId, int action) {
		this.guestUserId = guestUserId;
		this.actionParam = action;
		favouriteBoardList = new ListView(context);
		favouriteBoardList.setDividerHeight(0);
		favouriteBoardList.setAdapter(new FavBoardListAdapter(boardlist));
		favouriteBoardList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				BoardObject board = boardlist.get(position);
				Intent intent = new Intent(context, DocListActivity.class);
				intent.putExtra("board", board);
				intent.putExtra("action", actionParam);
				context.startActivity(intent);
			}
		});
		refresh();
		this.addListView(favouriteBoardList);
		return this.gestureOverlayView;
	}
	
	 
	/**
	 * 获取收藏列表
	 */
	public List<BoardObject> getBoardlist() {
		return boardlist;
	}

	/**
	 * 识别手势
	 */
	protected void onGestureActionRecognized(String actionName, final int position) {
		if ("minus".equals(actionName)) {
			if (position >= 0 && position < boardlist.size()) {
				new AlertDialog.Builder(context).setTitle("管理收藏")
					.setMessage("从收藏中删除["+boardlist.get(position).getName()+"]")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							if (actionParam != ActionFactory.DEFAULT_PARAM)
								delOtherFavBoard(boardlist.get(position));
							else
								delFavBoard(boardlist.get(position));
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
	
	private class FavBoardListAdapter extends BaseAdapter {
    	
    	private List<BoardObject> list;
    	
		public FavBoardListAdapter(List<BoardObject> list) {
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
				tv =  LayoutInflater.from(FavBoardView.this.context)
						.inflate(SettingActivity.displayHd(FavBoardView.this.context)?R.layout.board_list_hd:R.layout.board_list, parent, false);
			} else {
				tv =  convertView;
			}
			final TextView l = (TextView)tv.findViewById(R.id.boardListItem);
			l.setText("[" + list.get(position).getName() + "]" + list.get(position).getCh());
			l.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_on, 0, 0, 0);
			l.setCompoundDrawablePadding(5);
			l.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			if (position%2 == 1)
				tv.setBackgroundResource(R.drawable.welcome_selector1);
			else
				tv.setBackgroundResource(R.drawable.welcome_selector2);
//			tv.setLongClickable(true);
//			tv.setOnTouchListener(new OnTouchListener() {
//
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					GestureDetector gd = new GestureDetector(new BaseGesture() {
//						protected void onLeftToRightFling(MotionEvent e1, MotionEvent e2) {
//							l.setText("删除");
//						}
//					});
//					return gd.onTouchEvent(event);
//				}
//				
//			});
//			
			return tv;
		}
	}
}
