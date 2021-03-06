package top.defaults.gradientdrawabletuner;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GradientDrawableViewModel extends AndroidViewModel {

    private Resources resources;
    private MediatorLiveData<GradientDrawable> gradientDrawable = new MediatorLiveData<>();
    private MutableLiveData<GradientDrawableProperties> drawableProperties = new MutableLiveData<>();

    public GradientDrawableViewModel(@NonNull Application application) {
        super(application);
        resources = application.getResources();
        gradientDrawable.addSource(drawableProperties, properties ->
                gradientDrawable.setValue(new DrawableBuilder().properties(properties).build()));
        drawableProperties.setValue(GradientDrawableProperties.Factory.createDefault());
    }

    public MutableLiveData<GradientDrawable> getGradientDrawable() {
        return gradientDrawable;
    }

    public MutableLiveData<GradientDrawableProperties> getDrawableProperties() {
        return drawableProperties;
    }

    public interface Callback<T> {
        void onData(T data);
    }

    public void updateProperty(String propertyName, Object value) {
        try {
            tryField(propertyName, value);
        } catch (NoSuchFieldException e) {
            trySetter(propertyName, value);
        }
    }

    private void tryField(String propertyName, Object value) throws NoSuchFieldException {
        Field field = GradientDrawableProperties.class.getField(propertyName);
        updateProperties(properties -> {
            try {
                field.set(properties, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private void trySetter(String propertyName, Object value) {
        try {
            Method method = GradientDrawableProperties.class.getMethod(setter(propertyName), value.getClass());
            updateProperties(properties -> {
                try {
                    method.invoke(properties, value);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            });
        } catch (NoSuchMethodException ignored) { }
    }

    private String setter(String field) {
        if (TextUtils.isEmpty(field)) return null;
        return "set" + (field.substring(0, 1).toUpperCase() + field.substring(1));
    }

    public void updateProperties(Callback<GradientDrawableProperties> callback) {
        GradientDrawableProperties properties = drawableProperties.getValue();
        if (properties == null) properties = GradientDrawableProperties.Factory.createDefault();
        callback.onData(properties);
        drawableProperties.setValue(properties);
    }

    public void apply(GradientDrawableProperties properties) {
        drawableProperties.setValue(properties.copy());
    }

    @SuppressWarnings("unused")
    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
