/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmcy.medialib.utils.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

public class RxPermissions {

    static final String TAG = RxPermissions.class.getSimpleName();
    static final Object TRIGGER = new Object();

    @VisibleForTesting
//    static Lazy<RxPermissionsFragment> mRxPermissionsFragment;

//    public RxPermissions(@NonNull final Context context) {
//        this((Activity) context);
//    }
//
//    public RxPermissions(@NonNull final Activity activity) {
//        if(!(activity instanceof FragmentActivity)){
//            throw new ClassCastException("The parent class of an Activity must be FragmentActivity");
//        }
//        FragmentActivity fragmentActivity = (FragmentActivity) activity;
//        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getLazySingleton(fragmentActivity.getSupportFragmentManager());
//    }
//
//    public RxPermissions(@NonNull final Fragment fragment) {
//        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getLazySingleton(fragment.getChildFragmentManager());
//    }

    private static Lazy<RxPermissionsFragment> getPermissionsFragment(@NonNull final Activity activity){
        if(!(activity instanceof FragmentActivity)){
            throw new ClassCastException("The parent class of an Activity must be FragmentActivity");
        }
        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        return getLazySingleton(fragmentActivity.getSupportFragmentManager());
    }

    private static Lazy<RxPermissionsFragment> getPermissionsFragment(@NonNull final Fragment fragment){
        return getLazySingleton(fragment.getChildFragmentManager());
    }

    @NonNull
    private static Lazy<RxPermissionsFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new Lazy<RxPermissionsFragment>() {

            private RxPermissionsFragment rxPermissionsFragment;

            @Override
            public synchronized RxPermissionsFragment get() {
                if (rxPermissionsFragment == null) {
                    rxPermissionsFragment = getRxPermissionsFragment(fragmentManager);
                }
                return rxPermissionsFragment;
            }

        };
    }

    private static RxPermissionsFragment getRxPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        RxPermissionsFragment rxPermissionsFragment = findRxPermissionsFragment(fragmentManager);
        boolean isNewInstance = rxPermissionsFragment == null;
        if (isNewInstance) {
            rxPermissionsFragment = new RxPermissionsFragment();
            fragmentManager
                    .beginTransaction()
                    .add(rxPermissionsFragment, TAG)
                    .commitNow();
        }
        return rxPermissionsFragment;
    }

    private static RxPermissionsFragment findRxPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        return (RxPermissionsFragment) fragmentManager.findFragmentByTag(TAG);
    }

//    public void setLogging(boolean logging) {
//        mRxPermissionsFragment.get().setLogging(logging);
//    }

    /**
     * Map emitted items from the source observable into {@code true} if permissions in parameters
     * are granted, or {@code false} if not.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @SuppressWarnings("WeakerAccess")
    public static <T> ObservableTransformer<T, Boolean> ensure(final Activity activity, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(activity);
        return ensure(mRxPermissionsFragment, permissions);
    }

    /**
     * Map emitted items from the source observable into {@code true} if permissions in parameters
     * are granted, or {@code false} if not.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @SuppressWarnings("WeakerAccess")
    public static <T> ObservableTransformer<T, Boolean> ensure(final Fragment fragment, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(fragment);
        return ensure(mRxPermissionsFragment, permissions);
    }

    private static <T> ObservableTransformer<T, Boolean> ensure(Lazy<RxPermissionsFragment> mRxPermissionsFragment, final String... permissions) {
        return o -> request(mRxPermissionsFragment, o, permissions)
                // Transform Observable<Permission> to Observable<Boolean>
                .buffer(permissions.length)
                .flatMap((Function<List<Permission>, ObservableSource<Boolean>>) permissions1 -> {
                    if (permissions1.isEmpty()) {
                        // Occurs during orientation change, when the subject receives onComplete.
                        // In that case we don't want to propagate that empty list to the
                        // subscriber, only the onComplete.
                        return Observable.empty();
                    }
                    // Return true if all permissions are granted.
                    for (Permission p : permissions1) {
                        if (!p.granted) {
                            return Observable.just(false);
                        }
                    }
                    return Observable.just(true);
                });
    }

    /**
     * Map emitted items from the source observable into {@link Permission} objects for each
     * permission in parameters.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @SuppressWarnings("WeakerAccess")
    public static <T> ObservableTransformer<T, Permission> ensureEach(final Activity activity, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(activity);
        return o -> request(mRxPermissionsFragment, o, permissions);
    }

    /**
     * Map emitted items from the source observable into {@link Permission} objects for each
     * permission in parameters.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @SuppressWarnings("WeakerAccess")
    public static <T> ObservableTransformer<T, Permission> ensureEach(final Fragment fragment, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(fragment);
        return o -> request(mRxPermissionsFragment, o, permissions);
    }

    /**
     * Map emitted items from the source observable into one combined {@link Permission} object. Only if all permissions are granted,
     * permission also will be granted. If any permission has {@code shouldShowRationale} checked, than result also has it checked.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    public static <T> ObservableTransformer<T, Permission> ensureEachCombined(final Activity activity, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(activity);
        return o -> request(mRxPermissionsFragment, o, permissions)
                .buffer(permissions.length)
                .flatMap((Function<List<Permission>, ObservableSource<Permission>>) permissions1 -> {
                    if (permissions1.isEmpty()) {
                        return Observable.empty();
                    }
                    return Observable.just(new Permission(permissions1));
                });
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    @SuppressWarnings({"unused"})
    public static Observable<Boolean> request(final Activity activity, final String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(activity, permissions));
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    @SuppressWarnings({"unused"})
    public static Observable<Boolean> request(final Fragment fragment, final String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(fragment, permissions));
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    @SuppressWarnings({"unused"})
    public static Observable<Permission> requestEach(final Activity activity, final String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEach(activity, permissions));
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    @SuppressWarnings({"unused"})
    public static Observable<Permission> requestEach(final Fragment fragment, final String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEach(fragment, permissions));
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    public static Observable<Permission> requestEachCombined(final Activity activity, final String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEachCombined(activity, permissions));
    }

    private static Observable<Permission> request(Lazy<RxPermissionsFragment> mRxPermissionsFragment, final Observable<?> trigger, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }

        return oneOf(trigger, pending(mRxPermissionsFragment, permissions))
                .flatMap((Function<Object, Observable<Permission>>) o -> requestImplementation(mRxPermissionsFragment, permissions));
    }

    private static Observable<?> pending(Lazy<RxPermissionsFragment> mRxPermissionsFragment, final String... permissions) {
        for (String p : permissions) {
            if (!mRxPermissionsFragment.get().containsByPermission(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private static Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(trigger, pending);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static Observable<Permission> requestImplementation(Lazy<RxPermissionsFragment> mRxPermissionsFragment, final String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();

        // In case of multiple permissions, we create an Observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (String permission : permissions) {
            mRxPermissionsFragment.get().log("Requesting permission " + permission);
            if (isGranted(mRxPermissionsFragment, permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                list.add(Observable.just(new Permission(permission, true, false)));
                continue;
            }

            if (isRevoked(mRxPermissionsFragment, permission)) {
                // Revoked by a policy, return a denied Permission object.
                list.add(Observable.just(new Permission(permission, false, false)));
                continue;
            }

            PublishSubject<Permission> subject = mRxPermissionsFragment.get().getSubjectByPermission(permission);
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                mRxPermissionsFragment.get().setSubjectForPermission(permission, subject);
            }

            list.add(subject);
        }

        if (!unrequestedPermissions.isEmpty()) {
            String[] unrequestedPermissionsArray = unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]);
            requestPermissionsFromFragment(mRxPermissionsFragment, unrequestedPermissionsArray);
        }
        return Observable.concat(Observable.fromIterable(list));
    }

    /**
     * Invokes Activity.shouldShowRequestPermissionRationale and wraps
     * the returned value in an observable.
     * <p>
     * In case of multiple permissions, only emits true if
     * Activity.shouldShowRequestPermissionRationale returned true for
     * all revoked permissions.
     * <p>
     * You shouldn't call this method if all permissions have been granted.
     * <p>
     * For SDK &lt; 23, the observable will always emit false.
     */
    public static Observable<Boolean> shouldShowRequestPermissionRationale(final Activity activity, final String... permissions) {
        if (!isMarshmallow()) {
            return Observable.just(false);
        }
        return Observable.just(shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean shouldShowRequestPermissionRationaleImplementation(final Activity activity, final String... permissions) {
        Lazy<RxPermissionsFragment> mRxPermissionsFragment = getPermissionsFragment(activity);
        for (String p : permissions) {
            if (!isGranted(mRxPermissionsFragment, p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void requestPermissionsFromFragment(Lazy<RxPermissionsFragment> mRxPermissionsFragment, String[] permissions) {
        mRxPermissionsFragment.get().log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        mRxPermissionsFragment.get().requestPermissions(permissions);
    }

    /**
     * Returns true if the permission is already granted.
     * <p>
     * Always true if SDK &lt; 23.
     */
    private static boolean isGranted(Lazy<RxPermissionsFragment> mRxPermissionsFragment, String permission) {
        return !isMarshmallow() || mRxPermissionsFragment.get().isGranted(permission);
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * <p>
     * Always false if SDK &lt; 23.
     */
    private static boolean isRevoked(Lazy<RxPermissionsFragment> mRxPermissionsFragment, String permission) {
        return isMarshmallow() && mRxPermissionsFragment.get().isRevoked(permission);
    }

    private static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    void onRequestPermissionsResult(Lazy<RxPermissionsFragment> mRxPermissionsFragment, String[] permissions, int[] grantResults) {
        mRxPermissionsFragment.get().onRequestPermissionsResult(permissions, grantResults, new boolean[permissions.length]);
    }


    public static boolean lacksPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_DENIED;
    }

    @FunctionalInterface
    public interface Lazy<V> {
        V get();
    }

}
