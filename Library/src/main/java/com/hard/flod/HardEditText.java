package com.hard.flod;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;

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
 * 11、在写xml时能看到btn和label ✔
 * 12、setErr 图标的处理 ✔
 * 13、background选中动画
 * 14、Label 动态更换文字 ✔
 * 15、按钮误触问题 ✔
 * 16、设置全局动画时间 ✔
 * 17、高度或者宽度写太小会gg
 * 18、Label 文字换行  idea 有\n遇到分成多组。每组超过宽度后换行
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class HardEditText extends AppCompatEditText {

    private boolean DEBUG = true;

    private void log(String s) {
        if (DEBUG) {
            Log.d("HardEditText", s);
        }
    }

    private static final int DEFAULT_CLEAR_ICON = R.drawable.ic_clear_24dp;
    private static final int DEFAULT_VISIBLE_ICON = R.drawable.ic_visibility_24dp;
    private static final int DEFAULT_INVISIBLE_ICON = R.drawable.ic_visibility_off_24dp;

    private final int DEFAULT_BTN_SIZE = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_btnSize);
    private final int DEFAULT_BTN_PADDING = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_btnPadding);

    private final int DEFAULT_LABEL_TEXT_SIZE = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelTextSize);
    private final int DEFAULT_LABEL_PADDING_TOP = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelPaddingTop);
    private final int DEFAULT_LABEL_PADDING_BOTTOM = getResources().getDimensionPixelSize(R.dimen.HardEditText_default_labelPaddingBottom);

    private boolean enableClearBtn;
    private boolean enablePwVisibleBtn;
    private boolean enableHideWithClearBtn; //all btn will hide with clearBtn
    private boolean enableLabel;


    private Drawable mClearBtnDrawable;
    private Drawable mVisibleBtnDrawable;
    private Drawable mInvisibleBtnDrawable;
    private Drawable mBackground;

    private Bitmap mClearBtnBitmap;
    private Bitmap mVisibleBtnBitmap;
    private Bitmap mInvisibleBtnBitmap;

    private Rect mClearBtnRect = new Rect();
    private Rect mPwVisibleBtnRect = new Rect();
    private Rect mTextRect = new Rect();

    private int mBtnSize;           //all btn size
    private int mBtnPadding;        //all btn padding
    private int mBtnTranslationX;   //Horizontal Translation

    private String mLabelText;      //if no setLabelText,it default hintText
    private int mLabelTextSize;     //@Px
    private int mLabelTextColor;    //@ColorInt
    private int mLabelGravity;      //Gravity: left | center | right
    private int mLabelPaddingTop;
    private int mLabelPaddingBottom;
    private int mLabelTranslationX; //Horizontal Translation

    /**
     * @see #setPadding(int, int, int, int)
     * when view {@link #onFinishInflate}
     * save original padding and resetPadding for btn and label space need
     */
    private int mTextPaddingLeft;
    private int mTextPaddingRight;
    private int mTextPaddingTop;
    private int mTextPaddingBottom;


    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    /**
     * @see #setClearBtnVisible#setLabelVisible
     * they define current visibility
     */
    private boolean isClearBtnVisible;
    private boolean isPwBtnVisible;
    private boolean isLabelVisible;

    /**
     * @see #transformPasswordMode#isPasswordInputType(int)
     */
    private boolean isPasswordInputType;

    //for show and hide Animator
    private ValueAnimator mBtnAnimator;
    private ValueAnimator mLabelAnimator;

    //Animator Fraction [0,1]
    private float mBtnFraction;
    private float mLabelFraction;

    /**
     * @see #setError
     */
    private boolean isErr;


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
        enablePwVisibleBtn = array.getBoolean(R.styleable.HardEditText_enablePwVisibleBtn, false);
        enableHideWithClearBtn = array.getBoolean(R.styleable.HardEditText_enableHideWithClearBtn, true);
        mBtnSize = array.getDimensionPixelSize(R.styleable.HardEditText_btnSize, DEFAULT_BTN_SIZE);
        mBtnPadding = array.getDimensionPixelSize(R.styleable.HardEditText_btnPadding, DEFAULT_BTN_PADDING);
        int mBtnColor = array.getColor(R.styleable.HardEditText_btnColor, -1);
        mBtnTranslationX = array.getDimensionPixelSize(R.styleable.HardEditText_btnTranslationX, 0);


        int clearBtnResId = array.getResourceId(R.styleable.HardEditText_clearBtnSrc, DEFAULT_CLEAR_ICON);
        int visibleBtnResId = array.getResourceId(R.styleable.HardEditText_visibleBtnSrc, DEFAULT_VISIBLE_ICON);
        int invisibleBtnResId = array.getResourceId(R.styleable.HardEditText_invisibleSrc, DEFAULT_INVISIBLE_ICON);
        int backgroundResId = array.getResourceId(R.styleable.HardEditText_background, -1);


        enableLabel = array.getBoolean(R.styleable.HardEditText_enableLabel, true);
        mLabelText = array.getString(R.styleable.HardEditText_labelText);
        mLabelTextSize = array.getDimensionPixelSize(R.styleable.HardEditText_labelTextSize, DEFAULT_LABEL_TEXT_SIZE);
        mLabelTextColor = array.getColor(R.styleable.HardEditText_labelTextColor, Color.parseColor("#757575"));
        mLabelPaddingTop = array.getDimensionPixelSize(R.styleable.HardEditText_labelPaddingTop, DEFAULT_LABEL_PADDING_TOP);
        mLabelPaddingBottom = array.getDimensionPixelSize(R.styleable.HardEditText_labelPaddingBottom, DEFAULT_LABEL_PADDING_BOTTOM);
        mLabelTranslationX = array.getDimensionPixelSize(R.styleable.HardEditText_labelTranslationX, 0);

        int animDuration = array.getInt(R.styleable.HardEditText_animDuration, 200);
        array.recycle();

        //getBitmap and setBitmapColor
        mClearBtnBitmap = getBitmap(context, clearBtnResId, mBtnColor);
        mVisibleBtnBitmap = getBitmap(context, visibleBtnResId, mBtnColor);
        mInvisibleBtnBitmap = getBitmap(context, invisibleBtnResId, mBtnColor);
        if (backgroundResId > 0) mBackground = ContextCompat.getDrawable(context, backgroundResId);

        isPasswordInputType = isPasswordInputType(getInputType());
        if (enablePwVisibleBtn && !enableHideWithClearBtn)
            isPwBtnVisible = true; //PwVisibleBtn always show


        mBtnAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(animDuration);
        mBtnAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Fraction value is [0.0,1.0]
                mBtnFraction = animation.getAnimatedFraction();

            }
        });

        mLabelAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(animDuration);
        mLabelAnimator.setInterpolator(new OvershootInterpolator());
        mLabelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLabelFraction = animation.getAnimatedFraction();

            }
        });


        //if mLabelText is null. it will be hintText;
        if (mLabelText == null && getHint() != null) mLabelText = getHint().toString();
        mLabelGravity = getGravity();

        if (getRootView().isInEditMode()) {
            //Preview
            isClearBtnVisible = true;
            isLabelVisible = true;
            isPwBtnVisible = true;
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //onFocus the EditText will be keep draw,the btn will be gone if no draw.
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
            //Anim part
            drawClearBtn(mBtnFraction, canvas);
            drawVisibleBtn(enableHideWithClearBtn ? mBtnFraction : 1, canvas);
            invalidate = true;
        } else {
            //Static part
            drawClearBtn(isClearBtnVisible ? 1 : 0, canvas);
            drawVisibleBtn(isPwBtnVisible ? 1 : 0, canvas);
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
            // if EditText is singleLine ,when the text over the width,EditText will scroll left
            // so we need add ScrollX
            mClearBtnRect.right = (int) (getWidth() + getScrollX() + mBtnTranslationX
                    - mBtnPadding - mTextPaddingRight - (mBtnSize - mBtnSize * animValue) / 2);
            if (enablePwVisibleBtn) //if we have the PwVisibleBtn at right,add one mBtnSize and padding
                mClearBtnRect.right -= mBtnSize + mBtnPadding;
            mClearBtnRect.left = (int) (mClearBtnRect.right - mBtnSize * animValue);
            mClearBtnRect.top = (int) ((getHeight() + getLabelSpace() - mBtnSize * animValue) / 2 + getScrollY());
            mClearBtnRect.bottom = (int) (mClearBtnRect.top + mBtnSize * animValue);
            //src will crop original Bitmap，then the dst is after rect.
            canvas.drawBitmap(mClearBtnBitmap, null, mClearBtnRect, mPaint);
        }

    }

    private void drawVisibleBtn(float animValue, Canvas canvas) {
        if (enablePwVisibleBtn && animValue != 0) {
            mPwVisibleBtnRect.right = (int) (getWidth() + getScrollX() + mBtnTranslationX
                    - mBtnPadding - mTextPaddingRight - (mBtnSize - mBtnSize * animValue) / 2);
            mPwVisibleBtnRect.left = (int) (mPwVisibleBtnRect.right - mBtnSize * animValue);
            mPwVisibleBtnRect.top = (int) ((getHeight() + getLabelSpace() - mBtnSize * animValue) / 2 + getScrollY());
            mPwVisibleBtnRect.bottom = (int) (mPwVisibleBtnRect.top + mBtnSize * animValue);
            if (isPasswordInputType)
                canvas.drawBitmap(mInvisibleBtnBitmap, null, mPwVisibleBtnRect, mPaint);
            else canvas.drawBitmap(mVisibleBtnBitmap, null, mPwVisibleBtnRect, mPaint);
        }

    }

    private void drawLabel(float animValue, Canvas canvas) {
        if (enableLabel && mLabelText != null && animValue != 0) {
            int startX = mLabelTranslationX + getScrollX();
            //drawText start baseLine, so startY need sub MetricsTop
            int startY = (int) (mLabelPaddingTop - mTextPaint.getFontMetrics().top + (1 - animValue) * mLabelTextSize) + getScrollY();
            if ((mLabelGravity & Gravity.START) == Gravity.START) {
                //left
                startX += 0;
            } else if ((mLabelGravity & Gravity.END) == Gravity.END) {
                //right
                startX += (int) (getWidth() - mTextPaddingRight - mTextPaint.measureText(mLabelText));
            } else {
                //center
                startX += (int) (getWidth() - mTextPaint.measureText(mLabelText)) / 2;
            }

            mTextPaint.setAlpha((int) (255 * animValue));
            mTextPaint.setColor(mLabelTextColor);
            mTextPaint.setTextSize(mLabelTextSize);
            canvas.drawText(mLabelText, startX, startY, mTextPaint);
        }

    }


    private Bitmap getBitmap(Context context, int resId, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        return getBitmap(context, drawable, color);
    }

    private Bitmap getBitmap(Context context, Drawable drawable, int color) {
        if (drawable != null) {
            if (color != -1) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    //getOriginalBackground drawableState and set mBackground
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
        if (isErr){
            if (!enableHideWithClearBtn) isPwBtnVisible = true;
            isErr = false;
            setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        }

        if (text.length() > 0) {
            if (enableClearBtn && !isClearBtnVisible) setClearBtnVisible(true);
            if (enableLabel && !isLabelVisible) setLabelVisible(true);
        } else {
            if (enableClearBtn && isClearBtnVisible) setClearBtnVisible(false);
            if (enableLabel && isLabelVisible) setLabelVisible(false);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (enableClearBtn || enablePwVisibleBtn) {
                int visibleBtnRight = getWidth() + mBtnTranslationX - mBtnPadding - mTextPaddingRight;
                int clearBtnRight = visibleBtnRight;
                if (enablePwVisibleBtn)
                    clearBtnRight -= mBtnSize + mBtnPadding;
                boolean isClearBtnTouch = clearBtnRight > event.getX()
                        && clearBtnRight - mBtnSize < event.getX()
                        && getLabelSpace() + mTextPaddingTop < event.getY();
                boolean isPwVisibleBtnTouch = visibleBtnRight > event.getX()
                        && visibleBtnRight - mBtnSize < event.getX()
                        && getLabelSpace() + mTextPaddingTop < event.getY();

                //do not return true,otherwise we can't get backgroundDrawableState.
                if (enableClearBtn && isClearBtnVisible && isClearBtnTouch) {
                    setText("");
                } else if (enablePwVisibleBtn && isPwBtnVisible && isPwVisibleBtnTouch) {
                    isPasswordInputType = !isPasswordInputType;
                    transformPasswordMode(isPasswordInputType);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void setClearBtnVisible(boolean visible) {
        isClearBtnVisible = visible;
        if (enableHideWithClearBtn)
            isPwBtnVisible = visible;
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
        setSelection(getText() != null ? getText().length() : 0);
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


    /**
     * it will be true.then reset padding and hide btn
     */
    @Override
    public void setError(CharSequence error) {
        isErr = true;
        super.setPadding(mTextPaddingLeft, mTextPaddingTop + getLabelSpace(), mTextPaddingRight, mTextPaddingBottom);
        isClearBtnVisible = false;
        isPwBtnVisible = false;
        super.setError(error);
    }

    /**
     * save original padding and resetPadding for btn and label space need
     */
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mTextPaddingLeft = left;
        mTextPaddingTop = top;
        mTextPaddingRight = right;
        mTextPaddingBottom = bottom;
        super.setPadding(mTextPaddingLeft, mTextPaddingTop + getLabelSpace(),
                mTextPaddingRight + getBtnSpace(), mTextPaddingBottom);
    }


    public HardEditText setEnableLabel(boolean enable) {
        enableLabel = enable;
        return this;
    }

    /**
     * if labelText is no null,enableLabel will be true.otherwise enableLabel is false.
     *
     * @param playAnim play Show or hide anim
     */
    public HardEditText setLabelText(String labelText, boolean playAnim) {
        mLabelText = labelText;
        if (labelText != null) {
            enableLabel = true;
            if (playAnim) setLabelVisible(true);
        } else {
            enableLabel = false;
            if (playAnim) setLabelVisible(false);
        }
        return this;
    }

    /**
     * if labelText is no null,enableLabel will be true.otherwise enableLabel is false.
     * no anim
     */
    public HardEditText setLabelText(String labelText) {
        return setLabelText(labelText, false);
    }


    public HardEditText setLabelTextSize(@Px int textSize) {
        mLabelTextSize = textSize;
        return this;
    }

    public HardEditText setLabelTextColor(@ColorInt int color) {
        mLabelTextColor = color;
        return this;
    }

    public HardEditText setEnableClearBtn(boolean enableClearBtn) {
        this.enableClearBtn = enableClearBtn;
        return this;
    }

    public HardEditText setEnablePwVisibleBtn(boolean enablePwVisibleBtn) {
        this.enablePwVisibleBtn = enablePwVisibleBtn;
        return this;
    }

    public HardEditText setEnableHideWithClearBtn(boolean enableHideWithClearBtn) {
        this.enableHideWithClearBtn = enableHideWithClearBtn;
        return this;
    }

    public HardEditText setBackgroundNoLabel(Drawable background) {
        mBackground = background;
        return this;
    }

    public void setClearBtnRes(@DrawableRes int resId) {
        mClearBtnBitmap = getBitmap(getContext(), resId, -1);
    }

    public void setPwBtnVisibleRes(@DrawableRes int resId) {
        mVisibleBtnBitmap = getBitmap(getContext(), resId, -1);
    }

    public void setPwBtnInvisibleRes(@DrawableRes int resId) {
        mInvisibleBtnBitmap = getBitmap(getContext(), resId, -1);
    }


    public HardEditText setBtnSize(@Px int btnSize) {
        mBtnSize = btnSize;
        return this;
    }

    public HardEditText setBtnPadding(@Px int btnPadding) {
        mBtnPadding = btnPadding;
        return this;
    }

    public HardEditText setBtnColor(@ColorInt int btnColor) {
        mClearBtnBitmap = getBitmap(getContext(), mClearBtnDrawable, btnColor);
        mVisibleBtnBitmap = getBitmap(getContext(), mVisibleBtnDrawable, btnColor);
        mInvisibleBtnBitmap = getBitmap(getContext(), mInvisibleBtnDrawable, btnColor);
        return this;
    }

    public HardEditText setBtnTranslationX(@Px int btnTranslationX) {
        mBtnTranslationX = btnTranslationX;
        return this;
    }

    public HardEditText setLabelGravity(int labelGravity) {
        mLabelGravity = labelGravity;
        return this;
    }

    public HardEditText setLabelPaddingTop(@Px int labelPaddingTop) {
        mLabelPaddingTop = labelPaddingTop;
        return this;
    }

    public HardEditText setLabelPaddingBottom(@Px int labelPaddingBottom) {
        mLabelPaddingBottom = labelPaddingBottom;
        return this;
    }

    public HardEditText setLabelTranslationX(@Px int labelTranslationX) {
        mLabelTranslationX = labelTranslationX;
        return this;
    }


    /**
     * all Animator will set this value
     */
    public HardEditText setAnimDuration(int millisecond){
        mBtnAnimator.setDuration(millisecond);
        mLabelAnimator.setDuration(millisecond);
        return this;
    }

    public ValueAnimator getBtnAnimator() {
        return mBtnAnimator;
    }

    public ValueAnimator getLabelAnimator() {
        return mLabelAnimator;
    }
}
