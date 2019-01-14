package com.hard.flod;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-01-09
 * UseDes:
 * <p>
 * TODO
 * 1、一键清除功能  ✔
 * 2、密码可见与隐藏功能 ✔
 * 3、getEditPaddingRight() 计算的问题 ✔
 * 4、mBtnSize 的计算问题 ✔
 * 5、加上label后还需要再计算按钮垂直方向上的触摸判断 ✔
 * 6、scrollY的绘制问题✔
 * 7、Label 功能 ✔
 * 8、Label Color和Gravity  Gravity 暂时随EditText一样 ✔
 * 9、新写一个设置background的方法，为了将label放到外面来 ✔
 * 10、将background的states转移到自己定义background上   ✔
 * 11、高度或者宽度写太小会gg
 * 12、在写xml时能看到btn和label
 * 13、setErr 图标的处理
 * 14、setDrawable 自定义图标
 * 15、Label 文字换行
 * 16、background选中动画
 */
public class HardEditText extends AppCompatEditText {

    private boolean DEBUG = true;
    private static final int DEFAULT_CLEAR_ICON = R.drawable.ic_clear_black_24dp;
    private static final int DEFAULT_VISIBLE_ICON = R.drawable.ic_visibility_black_24dp;
    private static final int DEFAULT_INVISIBLE_ICON = R.drawable.ic_visibility_off_black_24dp;

    private final int DEFAULT_BTN_SIZE = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_btnSize);
    private final int DEFAULT_BTN_PADDING = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_btnPadding);

    private final int DEFAULT_LABEL_TEXT_SIZE = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelTextSize);
    private final int DEFAULT_LABEL_PADDING_TOP = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelPaddingTop);
    private final int DEFAULT_LABEL_PADDING_BOTTOM = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelPaddingBottom);


    private boolean enableClearBtn;          //开启清除文本按钮功能
    private boolean enablePwVisibleBtn;      //开启密码显示和隐藏功能
    private boolean enableHideWithClearBtn;  //是否和ClearBtn一起消失
    private boolean enableLabel;             //开启Label的功能

    private Bitmap mClearBtnBitmap;
    private Bitmap mVisibleBtnBitmap;
    private Bitmap mInvisibleBtnBitmap;
    private Drawable mBackground;

    private int mBtnSize;           //按钮的高度和宽度（限定为方形）
    private int mBtnPadding;
    private int mBtnTranslationX;   //Btn的水平偏移量

    private String mLabelText;      //label的文字内容
    private int mLabelTextSize;     //label的文字大小
    private int mLabelTextColor;    //label的文字颜色
    private int mLabelGravity;      //label的Gravity: left | center |right
    private int mLabelPaddingTop;   //label PaddingTop
    private int mLabelPaddingBottom;//label PaddingBottom（与文字）
    private int mLabelTranslationX; //label的水平偏移量


    private int mEditPaddingLeft;   //外部的padding
    private int mEditPaddingRight;  //外部的padding
    private int mEditPaddingTop;    //外部的padding
    private int mEditPaddingBottom; //外部的padding


    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);


    private boolean isClearBtnVisible;
    private boolean isPasswordInputType;
    private boolean isLabelVisible;

    private Rect mClearBtnRect = new Rect();
    private Rect mPwVisibleBtnRect = new Rect();
    private Rect mTextRect = new Rect();

    private ValueAnimator mBtnAnimator;     //按钮动画
    private ValueAnimator mLabelAnimator;   //Label动画
    private float mBtnFraction;
    private float mLabelFraction;


    private static final int VALUE_ANIMATOR_TIME = 200;  //全局动画时间


    public HardEditText(Context context) {
        super(context);
        init(context, null);
    }

    public HardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public HardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HardEditText);
        enableClearBtn = array.getBoolean(R.styleable.HardEditText_enableClearBtn, true);
        enablePwVisibleBtn = array.getBoolean(R.styleable.HardEditText_enableClearBtn, true);
        enableHideWithClearBtn = array.getBoolean(R.styleable.HardEditText_enableHideWithClearBtn, true);
        mBtnSize = array.getDimensionPixelSize(R.styleable.HardEditText_btnSize, DEFAULT_BTN_SIZE);
        mBtnPadding = array.getDimensionPixelSize(R.styleable.HardEditText_btnPadding, DEFAULT_BTN_PADDING);
        mBtnTranslationX = array.getDimensionPixelSize(R.styleable.HardEditText_btnTranslationX, 0);


        int clearBtnResId = array.getResourceId(R.styleable.HardEditText_clearBtnSrc, DEFAULT_CLEAR_ICON);
        int visibleBtnResId = array.getResourceId(R.styleable.HardEditText_visibleBtnSrc, DEFAULT_VISIBLE_ICON);
        int invisibleBtnResId = array.getResourceId(R.styleable.HardEditText_invisibleSrc, DEFAULT_INVISIBLE_ICON);
        int backgroundResId = array.getResourceId(R.styleable.HardEditText_background, -1);

        //if null. it will be hintText;
        enableLabel = array.getBoolean(R.styleable.HardEditText_enableLabel, true);
        mLabelText = array.getString(R.styleable.HardEditText_labelText);
        mLabelTextSize = array.getDimensionPixelSize(R.styleable.HardEditText_labelTextSize, DEFAULT_LABEL_TEXT_SIZE);
        mLabelTextColor = array.getColor(R.styleable.HardEditText_labelTextColor, Color.parseColor("#757575"));
        mLabelPaddingTop = array.getDimensionPixelSize(R.styleable.HardEditText_labelPaddingTop, DEFAULT_LABEL_PADDING_TOP);
        mLabelPaddingBottom = array.getDimensionPixelSize(R.styleable.HardEditText_labelPaddingBottom, DEFAULT_LABEL_PADDING_BOTTOM);
        mLabelTranslationX = array.getDimensionPixelSize(R.styleable.HardEditText_labelTranslationX, 0);

        if (mLabelText == null) {
            if (getHint() != null) {
                mLabelText = getHint().toString();
            }

        }

        array.recycle();


        //拿到三个Icon的bitmap
        mClearBtnBitmap = getBitmap(context, clearBtnResId);
        mVisibleBtnBitmap = getBitmap(context, visibleBtnResId);
        mInvisibleBtnBitmap = getBitmap(context, invisibleBtnResId);
        if (backgroundResId > 0) mBackground = ContextCompat.getDrawable(context, backgroundResId);

        isPasswordInputType = isPasswordInputType(getInputType());

        mBtnAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(VALUE_ANIMATOR_TIME);
        mBtnAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Fraction value is [0.0,1.0]
                mBtnFraction = animation.getAnimatedFraction();
            }
        });

        mLabelAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(VALUE_ANIMATOR_TIME);
        mLabelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLabelFraction = animation.getAnimatedFraction();
            }
        });

        mTextPaint.setColor(mLabelTextColor);
        mTextPaint.setTextSize(mLabelTextSize);
        mLabelGravity = getGravity();

        if (getRootView().isInEditMode()) {
            //Preview
            isClearBtnVisible = true;
            isPasswordInputType = true;
            isLabelVisible = true;
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //由于光标的闪烁和文字的输入，会不断地draw,会清空之前的图标
        //TODO maybe可以保存一下，减少重draw，提高性能
        drawBackground(canvas);
        drawBtnAndLabel(canvas);

        //draw original shit
        super.onDraw(canvas);
    }

    private void drawBackground(Canvas canvas) {
        if (mBackground != null) {
            mTextRect.left = getScrollX();
            mTextRect.top = getLabelSpace() + getScrollY();
            mTextRect.right = getWidth() + getScrollX();
            mTextRect.bottom = getHeight() + getScrollY();
            mBackground.setBounds(mTextRect);
            mBackground.draw(canvas);
        }
    }


    private void drawBtnAndLabel(Canvas canvas) {
        //drawClearBtn and drawVisibleBtn
        boolean invalidate = false;
        if (mBtnAnimator.isRunning()) {
            //动画部分
            drawClearBtn(mBtnFraction, canvas);
            if (enableHideWithClearBtn)
                drawVisibleBtn(mBtnFraction, canvas);
            else drawVisibleBtn(1, canvas);
            invalidate = true;
        } else {
            //静态部分
            drawClearBtn(isClearBtnVisible ? 1 : 0, canvas);
            if (enableHideWithClearBtn)
                drawVisibleBtn(isClearBtnVisible ? 1 : 0, canvas);
            else drawVisibleBtn(1, canvas);
        }

        //drawLabel
        if (mLabelAnimator.isRunning()) {
            drawLabel(mLabelFraction, canvas);
            invalidate = true;
        } else {
            drawLabel(isLabelVisible ? 1 : 0, canvas);
        }

        if (invalidate) invalidate();  //keep draw

    }

    private void drawClearBtn(float animValue, Canvas canvas) {
        if (enableClearBtn && animValue != 0) {
            //当singleLine 超出部分是会向左滑动，ScrollX增加，按钮要向右移动来抵消偏移
            mClearBtnRect.right = (int) (getWidth() + getScrollX() + mBtnTranslationX - mBtnPadding - mEditPaddingRight
                    - (mBtnSize - mBtnSize * animValue) / 2);
            if (enablePwVisibleBtn) //如果右侧有PwVisibleBtn就再向左偏移一个按钮的距离
                mClearBtnRect.right -= mBtnSize + mBtnPadding;
            mClearBtnRect.left = (int) (mClearBtnRect.right - mBtnSize * animValue);
            mClearBtnRect.top = (int) ((getHeight() + getLabelSpace() - mBtnSize * animValue) / 2 + getScrollY());
            mClearBtnRect.bottom = (int) (mClearBtnRect.top + mBtnSize * animValue);
            //Rect rect = new Rect(left, top, right, bottom);
            //src是对原图片裁剪，第二个是裁剪后的位置
            canvas.drawBitmap(mClearBtnBitmap, null, mClearBtnRect, mPaint);
        }

    }

    private void drawVisibleBtn(float animValue, Canvas canvas) {
        if (enablePwVisibleBtn && animValue != 0) {
            mPwVisibleBtnRect.right = (int) (getWidth() + getScrollX() + mBtnTranslationX - mBtnPadding - mEditPaddingRight
                    - (mBtnSize - mBtnSize * animValue) / 2);
            mPwVisibleBtnRect.left = (int) (mPwVisibleBtnRect.right - mBtnSize * animValue);
            mPwVisibleBtnRect.top = (int) ((getHeight() + getLabelSpace() - mBtnSize * animValue) / 2 + getScrollY());
            mPwVisibleBtnRect.bottom = (int) (mPwVisibleBtnRect.top + mBtnSize * animValue);
            if (isPasswordInputType)
                canvas.drawBitmap(mInvisibleBtnBitmap, null, mPwVisibleBtnRect, mPaint);
            else canvas.drawBitmap(mVisibleBtnBitmap, null, mPwVisibleBtnRect, mPaint);
        }

    }

    private void drawLabel(float animValue, Canvas canvas) {
        if (enableLabel && animValue != 0) {
            int startX = mLabelTranslationX + getScrollX();
            //drawText 是从baseLine开始，需要向下移动 baseLine-top
            int startY = (int) (mLabelPaddingTop - mTextPaint.getFontMetrics().top + (1 - animValue) * mLabelTextSize) + getScrollY();
            if ((mLabelGravity & Gravity.START) == Gravity.START) {
                //left
                startX += mEditPaddingLeft;
            } else if ((mLabelGravity & Gravity.END) == Gravity.END) {
                //right
                startX += (int) (getWidth() - mEditPaddingRight - mTextPaint.measureText(mLabelText));
            } else {
                //center
                startX += (int) (getWidth() - mTextPaint.measureText(mLabelText)) / 2;
            }

            int alpha = (int) (255 * animValue);
            mTextPaint.setAlpha(alpha);

            canvas.drawText(mLabelText, startX, startY, mTextPaint);
        }

    }


    private Bitmap getBitmap(Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;

    }

    //将原本drawableState转移到自己定义的background
    @Override
    protected void drawableStateChanged() {
        if (mBackground != null && mBackground.isStateful()) {
            mBackground.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (enableClearBtn) {
            if (focused && getText() != null && getText().length() > 0) {
                if (!isClearBtnVisible) setClearBtnVisible(true);
            } else {
                if (isClearBtnVisible) setClearBtnVisible(false);
            }
        }

    }


    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (enableClearBtn) {
            if (text.length() > 0) {
                if (!isClearBtnVisible) setClearBtnVisible(true);
                if (!isLabelVisible) setLabelVisible(true);
            } else {
                if (isClearBtnVisible) setClearBtnVisible(false);
                if (isLabelVisible) setLabelVisible(false);
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (enableClearBtn || enablePwVisibleBtn) {
                int visibleBtnRight = getWidth() + mBtnTranslationX - mBtnPadding - mEditPaddingRight;
                int clearBtnRight = visibleBtnRight;
                if (enablePwVisibleBtn)
                    clearBtnRight -= mBtnSize + mBtnPadding;
                boolean isClearBtnTouch = clearBtnRight > event.getX()
                        && clearBtnRight - mBtnSize < event.getX()
                        && getLabelSpace() + mEditPaddingTop < event.getY();
                boolean isPwVisibleBtnTouch = visibleBtnRight > event.getX()
                        && visibleBtnRight - mBtnSize < event.getX()
                        && getLabelSpace() + mEditPaddingTop < event.getY();

                if (enableClearBtn && isClearBtnTouch) {
                    setText("");
                    //do not return true,otherwise can't getDrawableState().
                } else if (enablePwVisibleBtn && isPwVisibleBtnTouch) {
                    isPasswordInputType = !isPasswordInputType;
                    transformPasswordMode(isPasswordInputType);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void setClearBtnVisible(boolean visible) {
        isClearBtnVisible = visible;
        if (visible) {
            mBtnAnimator.start();   //mBtnFraction 0->1
        } else {
            mBtnAnimator.reverse(); //mBtnFraction 1->0
        }
        invalidate();
    }


    private void transformPasswordMode(boolean isPwMode) {
        if (isPwMode) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            setTransformationMethod(null);
        }
        setSelection(getText().length());
        this.isPasswordInputType = isPwMode;
        invalidate();
    }

    private void setLabelVisible(boolean visible) {
        isLabelVisible = visible;
        if (visible) {
            mLabelAnimator.start();
        } else {
            mLabelAnimator.reverse();
        }
        invalidate();
    }


    private boolean isPasswordInputType(int inputType) {
        final int variation =
                inputType & (InputType.TYPE_MASK_CLASS | InputType.TYPE_MASK_VARIATION);
        return variation
                == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }


    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mEditPaddingLeft = left;
        mEditPaddingTop = top;
        mEditPaddingRight = right;
        mEditPaddingBottom = bottom;
        super.setPadding(mEditPaddingLeft, mEditPaddingTop + getLabelSpace(),
                mEditPaddingRight + getBtnSpace(), mEditPaddingBottom);
    }


    private int getBtnSpace() {
        int width = 0;
        if (enablePwVisibleBtn) {
            width += mBtnPadding + mBtnSize;
        }
        if (enableClearBtn) {
            width += mBtnPadding + mBtnSize;
        }
        if (enablePwVisibleBtn || enableClearBtn)
            width += mBtnPadding;
        return width;
    }

    private int getLabelSpace() {
        return enableLabel ? mLabelTextSize + mLabelPaddingTop + mLabelPaddingBottom : 0;
    }


    private void log(String s) {
        if (DEBUG) {
            Log.d("HardEditText", s);
        }
    }

}
