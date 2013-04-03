package br.com.cybereagle.androidlibrary.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.com.cybereagle.commonlibrary.util.ReflectionUtils;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import br.com.cybereagle.androidlibrary.annotation.Retained;
import br.com.cybereagle.androidlibrary.util.Utils;

public abstract class EagleActivity extends TrackedFragmentActivity {

	protected String activityIdentifier;

	@InjectView(android.R.id.content)
	protected View contentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeUnretainedInstanceFields(savedInstanceState);
		@SuppressWarnings("unchecked")
		final Map<String, Object> retainedMap = (Map<String, Object>) getLastCustomNonConfigurationInstance();
		if (retainedMap == null) {
			initializeRetainedInstanceFields(savedInstanceState);
		} else {
			reinitializeInstanceFields(retainedMap);
		}
		createView(savedInstanceState);
		setupCloseSoftKeyboardListener(contentView);
		contentView.setFocusable(true);
		contentView.setFocusableInTouchMode(true);
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		Map<String, Object> retainedMap = new HashMap<String, Object>();
		Class<? extends EagleActivity> clazz = this.getClass();
		List<Field> fields = ReflectionUtils.getInstanceVariables(clazz);
		for (Field field : fields) {
			if (field.getAnnotation(Retained.class) != null) {
				try {
					retainedMap.put(field.getName(), ReflectionUtils.get(field, this));
				} catch (InvocationTargetException e) {
					logException(e);
				}
			}
		}
		return retainedMap;
	}

	protected void reinitializeInstanceFields(Map<String, Object> retainedMap) {
		Class<? extends EagleActivity> clazz = this.getClass();
		List<Field> fields = ReflectionUtils.getInstanceVariables(clazz);
		for (Field field : fields) {
			if (field.getAnnotation(Retained.class) != null) {
				try {
					ReflectionUtils.set(field, this, retainedMap.get(field.getName()));
				} catch (InvocationTargetException e) {
					logException(e);
				}
			}
		}
	}

	protected void logException(Exception e) {
		Log.d(getActivityIdentifier(), e.getLocalizedMessage());
	}

	protected abstract void initializeRetainedInstanceFields(Bundle savedInstanceState);

	protected abstract void initializeUnretainedInstanceFields(Bundle savedInstanceState);

	protected abstract void createView(Bundle savedInstanceState);

	protected String getActivityIdentifier() {
		if (this.activityIdentifier != null) {
			return this.activityIdentifier;
		}
		String className = this.getClass().getSimpleName();
		StringBuilder activityIdentifier = new StringBuilder();

		String[] subNames = className.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (int i = 0; i < subNames.length; i++) {
			activityIdentifier.append((i == 0 ? "" : "_") + subNames[i]);
		}
		this.activityIdentifier = activityIdentifier.toString().toUpperCase(Locale.US);
		return this.activityIdentifier;
	}

	private void setupCloseSoftKeyboardListener(View view) {
		// Set up touch listener for non-text box views to hide keyboard.
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Utils.hideSoftKeyboard(EagleActivity.this);
				View focusedView = getCurrentFocus();
				if(focusedView != null){
					focusedView.clearFocus();
				}
				return false;
			}

		});
	}
}
