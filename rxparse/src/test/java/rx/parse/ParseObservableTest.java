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

import com.parse.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import bolts.Continuation;
import bolts.Task;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParseObservableTest {

    @Test
    public void testParseObservableAllNextAfterCompleted() {
        ParseUser user = mock(ParseUser.class);
        ParseUser user2 = mock(ParseUser.class);
        ParseUser user3 = mock(ParseUser.class);
        List<ParseUser> users = new ArrayList<>();
        users.add(user);
        users.add(user2);
        users.add(user3);

        ParseQuery<ParseUser> query = (ParseQuery<ParseUser>) mock(ParseQuery.class);
        when(query.countInBackground()).thenReturn(Task.forResult(users.size()));
        when(query.findInBackground()).thenReturn(Task.forResult(users));
        when(query.setSkip(any(int.class))).thenReturn(null);
        when(query.setLimit(any(int.class))).thenReturn(null);
        when(user.getObjectId()).thenReturn("1_" + user.hashCode());
        when(user2.getObjectId()).thenReturn("2_" + user2.hashCode());
        when(user3.getObjectId()).thenReturn("3_" + user3.hashCode());

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.all(query))
            .withoutErrors()
            .expectedValues(user, user2, user3)
            .completes();
    }

    @Test
    public void testParseObservableFindNextAfterCompleted() {
        ParseUser user = mock(ParseUser.class);
        ParseUser user2 = mock(ParseUser.class);
        ParseUser user3 = mock(ParseUser.class);
        List<ParseUser> users = new ArrayList<>();
        users.add(user);
        users.add(user2);
        users.add(user3);

        Task<List<ParseUser>> task = Task.forResult(users);

        ParseQuery<ParseUser> query = (ParseQuery<ParseUser>) mock(ParseQuery.class);
        when(query.findInBackground()).thenReturn(task);

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.find(query))
            .withoutErrors()
            .expectedValues(user, user2, user3)
            .completes();
    }

    @Test
    public void testBlockingFind() {
        List<ParseUser> users = Arrays.asList(mock(ParseUser.class), mock(ParseUser.class), mock(ParseUser.class));
        ParseQuery<ParseUser> query = (ParseQuery<ParseUser>) mock(ParseQuery.class);
        when(query.findInBackground())
            .thenReturn(Task.forResult(users));
        try {
            when(query.find()).thenReturn(users);

            assertEquals(query.find(), rx.parse.ParseObservable.find(query).toList().toBlocking().single());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
