package com.example.listviewitemslidedeletebtnshow;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

public class QQListView extends ListView
{

	private static final String TAG = "QQlistView";

	// private static final int VELOCITY_SANP = 200;
	// private VelocityTracker mVelocityTracker;
	/**
	 * 用户滑动的最小距离
	 */
	private int touchSlop;

	/**
	 * 是否响应滑动
	 */
	private boolean isSliding;

	/**
	 * 手指按下时的x坐标
	 */
	private int xDown;
	/**
	 * 手指按下时的y坐标
	 */
	private int yDown;
	/**
	 * 手指移动时的x坐标
	 */
	private int xMove;
	/**
	 * 手指移动时的y坐标
	 */
	private int yMove;

	private LayoutInflater mInflater;

	private PopupWindow mPopupWindow;
	private int mPopupWindowHeight;
	private int mPopupWindowWidth;

	private Button mDelBtn;
	/**
	 * 为删除按钮提供一个回调接口
	 */
	private DelButtonClickListener mListener;

	/**
	 * 当前手指触摸的View
	 */
	private View mCurrentView;

	/**
	 * 当前手指触摸的位置
	 */
	private int mCurrentViewPos;

	/**
	 * 必要的一些初始化
	 * 
	 * @param context
	 * @param attrs
	 */
	public QQListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mInflater = LayoutInflater.from(context);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		View view = mInflater.inflate(R.layout.delete_btn, null);
		mDelBtn = (Button) view.findViewById(R.id.id_item_btn);
		mPopupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		/**
		 * 先调用下measure,否则拿不到宽和高
		 */
		mPopupWindow.getContentView().measure(0, 0);
		mPopupWindowHeight = mPopupWindow.getContentView().getMeasuredHeight();
		mPopupWindowWidth = mPopupWindow.getContentView().getMeasuredWidth();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (action)
		{

		case MotionEvent.ACTION_DOWN:
			xDown = x;
			yDown = y;
			/**
			 * 如果当前popupWindow显示，则直接隐藏，然后屏蔽ListView的touch事件的下传
			 */
			if (mPopupWindow.isShowing())
			{
				dismissPopWindow();
				return false;
			}
			// 获得当前手指按下时的item对应的数据在数据源模型中的索引
			mCurrentViewPos = pointToPosition(xDown, yDown);
			// 获得当前手指按下时的item      getFirstVisiblePosition() 返回当前屏幕上第一个可见的item的数据在数据源模型中的索引
			View view = getChildAt(mCurrentViewPos - getFirstVisiblePosition());
			mCurrentView = view;
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = x;
			yMove = y;
			int dx = xMove - xDown;
			int dy = yMove - yDown;
			/**
			 * 判断是否是从右到左的滑动
			 */
			if (xMove < xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop)
			{
				// Log.e(TAG, "touchslop = " + touchSlop + " , dx = " + dx +
				// " , dy = " + dy);
				isSliding = true;
			}
			break;
		}

		boolean state = super.dispatchTouchEvent(ev);

		Log.i(TAG, "dispatchTouchEvent = " + ev.getAction() + ", return = " +state);
		/**
		 * 	通过查看源码super.dispatchTouchEvent(ev)直接调用的是ViewGroup#dispatchTouchEvent(ev)
		 * 	ViewGroup#onFilterTouchEventForSecurity() 过滤触摸事件到应用的安全策略
		 * 	如果是ACTION_DOWN事件就取消状态并清除触摸响应对象 重置触摸状态
		 * 	如果是ACTION_DOWN事件或者触控目标链表中第一次触控的目标不为空,调用onInterceptTouchEvent(ev)进行拦截处理
		 * 	如果没有拦截且没有取消，中途会调用dispatchTransformedTouchEvent()它的返回值会作为QQListView#dispatchTouchEvent()的返回值
		 *
		 * 	在dispatchTransformedTouchEvent()中如果已经拦截或者复位取消接下来标志就调用当前View对象的super.dispatchTouchEvent(event)
		 *否则就调用当前View对象的dispatchTouchEvent(event) 后面的情况和刚才的处理逻辑类似；View#dispatchTouchEvent()中,会先处理onTouch()事件
		 *如果处理了onTouch()事件就返回true,那么就不会执行到onTouchEvent(event)，所以我们一般设置了OnTouchListener事件监听就把返回值设置为false,
		 *这样才能执行到onTouchEvent(event),执行onTouchEvent(event)后如果执行了单击等事件返回true，否则返回false
		 *
		 */

		return state;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		/**
		 * 如果是从右到左的滑动才相应
		 */
		if (isSliding)
		{
			switch (action)
			{
			case MotionEvent.ACTION_MOVE:

				int[] location = new int[2];
				// 获得当前item的位置x与y
				mCurrentView.getLocationOnScreen(location);
				// 设置popupWindow的动画
				mPopupWindow.setAnimationStyle(R.style.popwindow_delete_btn_anim_style);
				mPopupWindow.update();
				mPopupWindow.showAtLocation(mCurrentView, Gravity.LEFT | Gravity.TOP,
						location[0] + mCurrentView.getWidth(), location[1] + mCurrentView.getHeight() / 2
								- mPopupWindowHeight / 2);

				Log.i(TAG, "location[0]=" + location[0] + " ,location[1] = " + location[1] + ", mCurrentView.getWidth() = " + mCurrentView.getWidth() + " , mCurrentView.getHeight() = " + mCurrentView.getHeight() + " , mPopupWindowHeight = " + mPopupWindowHeight);

				// 设置删除按钮的回调
				mDelBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if (mListener != null)
						{
							mListener.clickHappend(mCurrentViewPos);
							mPopupWindow.dismiss();
						}
					}
				});
				// Log.e(TAG, "mPopupWindow.getHeight()=" + mPopupWindowHeight);

				break;
			case MotionEvent.ACTION_UP:
				isSliding = false;

			}
			// 相应滑动期间屏幕itemClick事件，避免发生冲突
			return true;
		}

		/**
		 * super.onTouchEvent(ev)这里调用的是AbsListView重写的onTouchEvent(ev)是返回true的
		 */
		boolean state = super.onTouchEvent(ev);
		Log.i(TAG, "state = " + state + ", action = " + ev.getAction());

		return state;
	}

	/**
	 * 隐藏popupWindow
	 */
	private void dismissPopWindow()
	{
		if (mPopupWindow != null && mPopupWindow.isShowing())
		{
			mPopupWindow.dismiss();
		}
	}

	public void setDelButtonClickListener(DelButtonClickListener listener)
	{
		mListener = listener;
	}

	interface DelButtonClickListener
	{
		public void clickHappend(int position);
	}

}
