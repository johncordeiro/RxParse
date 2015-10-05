/*
 * Copyright (C) 2015 8tory, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx.parse;

import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;

import com.parse.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.bolts.TaskObservable;

import android.app.Activity;
import android.content.Intent;

public class ParseObservable {

    public static <R extends ParseObject> Observable<R> find(ParseQuery<R> query) {
        return TaskObservable.defer(() -> query.findInBackground())
                .flatMap(l -> Observable.from(l))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    public static <R extends ParseObject> Observable<Integer> count(ParseQuery<R> query) {
        return TaskObservable.defer(() -> query.countInBackground())
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));

    }

    public static <R extends ParseObject> Observable<R> pin(R object) {
        return TaskObservable.deferNullable(() -> object.pinInBackground())
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> pin(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.pinAllInBackground(objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> pin(String name, R object) {
        return TaskObservable.deferNullable(() -> object.pinInBackground(name))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> pin(String name, List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.pinAllInBackground(name, objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> unpin(R object) {
        return TaskObservable.deferNullable(() -> object.unpinInBackground())
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> unpin(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.unpinAllInBackground(objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> unpin(String name, R object) {
        return TaskObservable.deferNullable(() -> object.unpinInBackground(name))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> unpin(String name, List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.unpinAllInBackground(name, objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> all(ParseQuery<R> query) {
        return count(query).flatMap(c -> all(query, c));
    }

    /** limit 10000 by skip */
    public static <R extends ParseObject> Observable<R> all(ParseQuery<R> query, int count) {
        final int limit = 1000; // limit limitation
        query.setSkip(0);
        query.setLimit(limit);
        Observable<R> find = find(query);
        for (int i = limit; i < count; i+= limit) {
            if (i >= 10000) break; // skip limitation
            query.setSkip(i);
            query.setLimit(limit);
            find.concatWith(find(query));
        }
        return find.distinct(o -> o.getObjectId());
    }

    public static <R extends ParseObject> Observable<R> first(ParseQuery<R> query) {
        return TaskObservable.defer(() -> query.getFirstInBackground())
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    public static <R extends ParseObject> Observable<R> get(Class<R> clazz, String objectId) {
        ParseQuery<R> query = ParseQuery.getQuery(clazz);
        return TaskObservable.defer(() -> query.getInBackground(objectId))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    // Task<T> nullable?
    public static <R> Observable<R> callFunction(String name, Map<String, ?> params) {
        return TaskObservable.deferNullable(() -> ParseCloud.callFunctionInBackground(name, params));
    }

    public static <R extends ParseObject> Observable<R> save(R object) {
        return TaskObservable.deferNullable(() -> object.saveInBackground())
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> save(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.saveAllInBackground(objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> saveEventually(R object) {
        return TaskObservable.deferNullable(() -> object.saveEventually())
                .map(v -> object);
    }

    // Task<T> nullable?
    public static <R extends ParseObject> Observable<R> fetch(R object) {
        return TaskObservable.deferNullable(() -> object.fetchInBackground())
                .map(v -> object);
    }

    // Task<List<T>> nullable?
    public static <R extends ParseObject> Observable<R> fetch(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.fetchAllInBackground(objects))
                .flatMap(l -> Observable.from(l)); // v -> Observable.from(objects)
    }

    // Task<T> nullable?
    public static <R extends ParseObject> Observable<R> fetchIfNeeded(R object) {
        return TaskObservable.deferNullable(() -> object.fetchIfNeededInBackground())
                .map(v -> object);
    }

    // Task<List<T>> nullable?
    public static <R extends ParseObject> Observable<R> fetchIfNeeded(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.fetchAllIfNeededInBackground(objects))
                .flatMap(l -> Observable.from(l)); // v -> Observable.from(objects)
    }

    // Task<T> nullable?
    public static <R extends ParseObject> Observable<R> delete(R object) {
        return TaskObservable.deferNullable(() -> object.deleteInBackground())
                .map(v -> object);
    }

    // Task<List<T>> nullable?
    public static <R extends ParseObject> Observable<R> delete(List<R> objects) {
        return TaskObservable.deferNullable(() -> ParseObject.deleteAllInBackground(objects))
                .flatMap(v -> Observable.from(objects));
    }

    public static Observable<String> subscribe(String channel) {
        android.util.Log.d("ParseObservable", "subscribe: channel: " + channel);

        return TaskObservable.deferNullable(() -> ParsePush.subscribeInBackground(channel))
                .doOnNext(v -> android.util.Log.d("ParseObservable", "doOnNext: " + v))
                .map(v -> channel);
    }

    public static Observable<String> unsubscribe(String channel) {
        android.util.Log.d("ParseObservable", "unsubscribe, channel: " + channel);

        return TaskObservable.deferNullable(() -> ParsePush.unsubscribeInBackground(channel))
                .map(v -> channel);
    }

    /* ParseFacebookUtils 1.8 */

    public static Observable<ParseUser> link(ParseUser user, Activity activity) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, activity))
                .map(v -> user);
    }

    public static Observable<ParseUser> link(ParseUser user, Activity activity, int activityCode) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, activity, activityCode))
                .map(v -> user);
    }

    public static Observable<ParseUser> link(ParseUser user, Collection<String> permissions, Activity activity) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, permissions, activity))
                .map(v -> user);
    }

    public static Observable<ParseUser> link(ParseUser user, Collection<String> permissions, Activity activity, int activityCode) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, permissions, activity, activityCode))
                .map(v -> user);
    }

    public static Observable<ParseUser> link(ParseUser user, String facebookId, String accessToken, Date expirationDate) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, facebookId, accessToken, expirationDate))
                .map(v -> user);
    }

    // Task<ParseUser> nullable?
    public static Observable<ParseUser> logIn(Collection<String> permissions, Activity activity, int activityCode) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInInBackground(permissions, activity, activityCode));
    }

    public static Observable<ParseUser> logIn(Collection<String> permissions, Activity activity) {
        // package class com.parse.FacebookAuthenticationProvider.DEFAULT_AUTH_ACTIVITY_CODE
        // private com.facebook.android.Facebook.DEFAULT_AUTH_ACTIVITY_CODE = 32665
        return logIn(permissions, activity, 32665);
    }

    // Task<ParseUser> nullable?
    public static Observable<ParseUser> logIn(String facebookId, String accessToken, Date expirationDate) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInInBackground(facebookId, accessToken, expirationDate));
    }

    public static Observable<ParseUser> saveLatestSessionData(ParseUser user) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.saveLatestSessionDataInBackground(user))
                .map(v -> user);
    }

    public static Observable<ParseUser> unlink(ParseUser user) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.unlinkInBackground(user))
                .map(v -> user);
    }

    /* ParsePush */

    // TODO send(JSONObject data, ParseQuery<ParseInstallation> query)
    // TODO send()
    // TODO sendMessage(String message)

    /* ParseObject */

    // TODO refresh()
    // TODO fetchFromLocalDatastore()

    /* ParseUser */

    public static Observable<ParseUser> become(String sessionToken) {
        return TaskObservable.defer(() -> ParseUser.becomeInBackground(sessionToken));
    }

    // TODO enableRevocableSessionInBackground

    // Task<ParseUser> nullable?
    public static Observable<ParseUser> logIn(String username, String password) {
        return TaskObservable.deferNullable(() -> ParseUser.logInInBackground(username, password));
    }

    // Task<ParseUser> nullable?
    public static Observable<Void> logOut() {
        return TaskObservable.deferNullable(() -> ParseUser.logOutInBackground());
    }

    // TODO requestPasswordResetInBackground(String email)
    // TODO signUpInBackground()

    // ParseAnalytics

    public static Observable<Intent> trackAppOpened(Intent intent) {
        return TaskObservable.deferNullable(() -> ParseAnalytics.trackAppOpenedInBackground(intent)).map(v -> intent);
    }

    public static Observable<String> trackEvent(String name) {
        return TaskObservable.deferNullable(() -> ParseAnalytics.trackEventInBackground(name)).map(v -> name);
    }

    public static Observable<String> trackEvent(String name, Map<String,String> dimensions) {
        return TaskObservable.deferNullable(() -> ParseAnalytics.trackEventInBackground(name, dimensions)).map(v -> name);
    }

}
