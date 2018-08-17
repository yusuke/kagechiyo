/*
 * Copyright 2018 Yusuke Yamamoto
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import java.util.ArrayList;
import java.util.List;

public class RestStream {
    private List<UserStreamListener> listeners = new ArrayList<>();
    private Twitter twitter;
    private boolean shutdown = false;

    public RestStream(Twitter twitter) {
        this.twitter = twitter;
    }
    public void addListener(UserStreamListener listener){
        listeners.add(listener);
    }


    public static <E> List<E> pickAdded(List<E> from, List<E> to) {
        ArrayList<E> added = new ArrayList<E>();
        for (E e : to) {
            if (!from.contains(e)) {
                added.add(e);
            }
        }
        return added;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int directMessageInterval = 60;

                List<DirectMessage> lastDicrectMessages = null;
                long lastDMChecked = System.currentTimeMillis();
                while (!shutdown) {
                    try {
                        // DMs
                        if (lastDicrectMessages == null) {
                            lastDicrectMessages = twitter.getDirectMessages(100);
                        }
                        if (System.currentTimeMillis() > (lastDMChecked + directMessageInterval * 1000)) {
                            lastDMChecked = System.currentTimeMillis();
                            List<DirectMessage> directMessages = twitter.getDirectMessages(100);
                            List<DirectMessage> newDMs = pickAdded(lastDicrectMessages, directMessages);
                            for (int i = newDMs.size() - 1; i >= 0; i--) {
                                DirectMessage e = newDMs.get(i);
                                for (UserStreamListener listener : listeners) {
                                    listener.onDirectMessage(e);
                                }
                            }
                            lastDicrectMessages = directMessages;

                        }
                        Thread.sleep(3000);
                    } catch (TwitterException e) {
                        int secondsUntilReset = e.getRateLimitStatus().getSecondsUntilReset();
                        try {
                            Thread.sleep(secondsUntilReset * 1000 + 1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int mentionsTimelineInterval = 12;

                long lastId = -1L;
                long lastMentionsTimelineChecked = System.currentTimeMillis();
                while (!shutdown) {
                    try {
                        // hometimeline
                        if (lastId == -1L) {
                            lastId = twitter.getMentionsTimeline().get(0).getId();
                        }
                        if (System.currentTimeMillis() > (lastMentionsTimelineChecked + mentionsTimelineInterval * 1000)) {
                            lastMentionsTimelineChecked = System.currentTimeMillis();

                            List<Status> mentionsTimeline = twitter.getMentionsTimeline(new Paging().count(100).sinceId(lastId));

                            for (int i = mentionsTimeline.size() - 1; i >= 0; i--) {
                                Status status = mentionsTimeline.get(i);
                                for (UserStreamListener listener : listeners) {
                                    listener.onStatus(status);
                                }
                            }
                            if (mentionsTimeline.size() > 0) {
                                lastId = mentionsTimeline.get(0).getId();
                            }
                        }
                        Thread.sleep(3000);
                    } catch (TwitterException e) {
                        int secondsUntilReset = e.getRateLimitStatus().getSecondsUntilReset();
                        try {
                            Thread.sleep(secondsUntilReset * 1000 + 1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    public void cleanUp() {

    }

    public void shutdown() {
        shutdown = true;

    }


}
