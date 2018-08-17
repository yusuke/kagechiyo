package twitter4j;

/*
 * Copyright 2014 Yusuke Yamamoto
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Kagechiyo {
    private final twitter4j.Twitter twitter = new twitter4j.TwitterFactory().getInstance();
    private final RestStream stream = new RestStream(twitter);
    private final Map<String, List<Consumer<Status>>> onMentionConsumerMap = new HashMap<>();
    private final Map<String, List<BiConsumer<Status, TwitterWrapper>>> onMentionBiConsumerMap = new HashMap<>();
    private final Map<String, List<Consumer<DirectMessage>>> onDirectMessageConsumerMap = new HashMap<>();
    private final Map<String, List<BiConsumer<DirectMessage, TwitterWrapper>>> onDirectMessageBiConsumerMap = new HashMap<>();

    private final List<Consumer<Status>> onMentionConsumersList = new ArrayList<>();
    private final List<BiConsumer<Status, TwitterWrapper>> onMentionBiConsumersList = new ArrayList<>();
    private final List<Consumer<DirectMessage>> onDirectMessageConsumersList = new ArrayList<>();
    private final List<BiConsumer<DirectMessage, TwitterWrapper>> onDirectMessageBiConsumersList = new ArrayList<>();

    public Kagechiyo() throws TwitterException {
        listener = new Listener(twitter);
        stream.addListener(listener);

    }

    public Kagechiyo(long botUserId) {
        listener = new Listener(botUserId);
        new RestStream(twitter).addListener(listener);

    }

    public Kagechiyo onMention(java.util.function.Consumer<Status> consumer) {
        onMentionConsumersList.add(consumer);
        return this;
    }

    public Kagechiyo onMention(java.util.function.BiConsumer<Status, TwitterWrapper> consumer) {
        onMentionBiConsumersList.add(consumer);
        return this;
    }

    public Kagechiyo onMention(String command, java.util.function.Consumer<Status> consumer) {
        onMentionConsumerMap.computeIfAbsent(command, e -> new ArrayList<>()).add(consumer);
        return this;
    }


    public Kagechiyo onMention(String command, java.util.function.BiConsumer<Status, TwitterWrapper> consumer) {
        onMentionBiConsumerMap.computeIfAbsent(command, e -> new ArrayList<>()).add(consumer);
        return this;
    }


    public Kagechiyo onDirectMessage(Consumer<DirectMessage> directMessageConsumer) {
        onDirectMessageConsumersList.add(directMessageConsumer);
        return this;
    }

    public Kagechiyo onDirectMessage(BiConsumer<DirectMessage, TwitterWrapper> directMessageConsumer) {
        onDirectMessageBiConsumersList.add(directMessageConsumer);
        return this;
    }

    public Kagechiyo onDirectMessage(String command, java.util.function.Consumer<DirectMessage> consumer) {
        onDirectMessageConsumerMap.computeIfAbsent(command, e -> new ArrayList<>()).add(consumer);
        return this;
    }

    public Kagechiyo onDirectMessage(String command, java.util.function.BiConsumer<DirectMessage, TwitterWrapper> consumer) {
        onDirectMessageBiConsumerMap.computeIfAbsent(command, e -> new ArrayList<>()).add(consumer);
        return this;
    }

    public class TwitterWrapper {
        long recipientId;
        Status originalStatus;
        DirectMessage originalDirectMessage;

        TwitterWrapper(Status status) {
            this.recipientId = status.getUser().getId();
            this.originalStatus = status;
            this.originalDirectMessage = null;
        }

        TwitterWrapper(DirectMessage message) {
            this.recipientId = message.getSenderId();
            this.originalStatus = null;
            this.originalDirectMessage = message;
        }

        public void updateStatus(String text) {
            try {
                twitter.updateStatus(text);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        public void reply(String text) {
            if (originalStatus != null) {
                try {
                    twitter.updateStatus(
                            new StatusUpdate(String.format("@%s %s", originalStatus.getUser().getScreenName(),
                                    text)).inReplyToStatusId(originalStatus.getId()));
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        }

        public void updateStatus(StatusUpdate status) {
            try {
                twitter.updateStatus(status);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        public void sendDirectMessage(String message) {
            try {
                twitter.sendDirectMessage(recipientId, message);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }


    public void start() {
        stream.start();
    }

    Listener listener;

    class Listener extends UserStreamAdapter {
        long myTwitterID;

        Listener(Twitter twitter) throws TwitterException {
            this.myTwitterID = twitter.verifyCredentials().getId();
        }

        Listener(long myTwitterID) {
            this.myTwitterID = myTwitterID;
        }

        @Override
        public void onStatus(Status status) {
            // guard condition to ignore tweets from myself
            if (status.getUser().getId() == this.myTwitterID) {
                return;
            }
            TwitterWrapper twitterWrapper = new TwitterWrapper(status);
            String[] split = status.getText().split(" ");
            for (UserMentionEntity userMentionEntity : status.getUserMentionEntities()) {
                if ((userMentionEntity.getId() == myTwitterID)) {
                    // call consumers only the status is a mention for the bot account
                    if (split.length >= 2) {
                        String command = split[1];
                        List<Consumer<Status>> statusConsumer = onMentionConsumerMap.get(command);
                        if (statusConsumer != null) {
                            for (Consumer<Status> consumer : statusConsumer) {
                                consumer.accept(status);
                            }
                        }

                        List<BiConsumer<Status, TwitterWrapper>> wrapperBiConsumer = onMentionBiConsumerMap.get(command);

                        if (wrapperBiConsumer != null) {
                            for (BiConsumer<Status, TwitterWrapper> statusTwitterWrapperBiConsumer : wrapperBiConsumer) {
                                statusTwitterWrapperBiConsumer.accept(status, twitterWrapper);
                            }
                        }
                    }
                    onMentionConsumersList.forEach(e -> e.accept(status));
                    onMentionBiConsumersList.forEach(e -> e.accept(status, twitterWrapper));
                    break;
                }
            }
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            String[] args = directMessage.getText().trim().split(" ");
            TwitterWrapper twitterWrapper = new TwitterWrapper(directMessage);
            if (args.length >= 1) {
                String command = args[0];
                List<Consumer<DirectMessage>> directMessageConsumer = onDirectMessageConsumerMap.get(command);
                if (directMessageConsumer != null) {
                    for (Consumer<DirectMessage> messageConsumer : directMessageConsumer) {
                        messageConsumer.accept(directMessage);
                    }
                }
                List<BiConsumer<DirectMessage, TwitterWrapper>> directMessageTwitterWrapperBiConsumer = onDirectMessageBiConsumerMap.get(command);
                if (directMessageTwitterWrapperBiConsumer != null) {
                    for (BiConsumer<DirectMessage, TwitterWrapper> messageTwitterWrapperBiConsumer : directMessageTwitterWrapperBiConsumer) {
                        messageTwitterWrapperBiConsumer.accept(directMessage, twitterWrapper);
                    }
                }
            }
            onDirectMessageConsumersList.forEach(e -> e.accept(directMessage));
            onDirectMessageBiConsumersList.forEach(e -> e.accept(directMessage, twitterWrapper));
        }
    }
}
