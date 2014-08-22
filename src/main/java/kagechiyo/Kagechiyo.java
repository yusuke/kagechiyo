package kagechiyo;

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

import twitter4j.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Kagechiyo {
    private final TwitterStream stream = new TwitterStreamFactory().getInstance();
    private final twitter4j.Twitter twitter = new twitter4j.TwitterFactory().getInstance();
    private final Map<String, Consumer<Status>> onMentionConsumerMap = new HashMap<>();
    private final Map<String, BiConsumer<Status, TwitterWrapper>> onMentionBiConsumerMap = new HashMap<>();
    private final Map<String, Consumer<DirectMessage>> onDirectMessageConsumerMap = new HashMap<>();
    private final Map<String, BiConsumer<DirectMessage, TwitterWrapper>> onDirectMessageBiConsumerMap = new HashMap<>();

    private final Set<Consumer<Status>> onMentionConsumersSet = new HashSet<>();
    private final Set<BiConsumer<Status, TwitterWrapper>> onMentionBiConsumersSet = new HashSet<>();
    private final Set<Consumer<DirectMessage>> onDirectMessageConsumersSet = new HashSet<>();
    private final Set<BiConsumer<DirectMessage, TwitterWrapper>> onDirectMessageBiConsumersSet = new HashSet<>();

    public Kagechiyo() throws TwitterException {
        listener = new Listener(TwitterFactory.getSingleton());
        stream.addListener(listener);

    }

    public Kagechiyo(long botUserId) {
        listener = new Listener(botUserId);
        stream.addListener(listener);

    }

    public Kagechiyo onMention(java.util.function.Consumer<Status> consumer) {
        onMentionConsumersSet.add(consumer);
        return this;
    }

    public Kagechiyo onMention(java.util.function.BiConsumer<Status, TwitterWrapper> consumer) {
        onMentionBiConsumersSet.add(consumer);
        return this;
    }

    public Kagechiyo onMention(String command, java.util.function.Consumer<Status> consumer) {
        onMentionConsumerMap.put(command, consumer);
        return this;
    }


    public Kagechiyo onMention(String command, java.util.function.BiConsumer<Status, TwitterWrapper> consumer) {
        onMentionBiConsumerMap.put(command, consumer);
        return this;
    }


    public Kagechiyo onDirectMessage(Consumer<DirectMessage> directMessageConsumer) {
        onDirectMessageConsumersSet.add(directMessageConsumer);
        return this;
    }

    public Kagechiyo onDirectMessage(BiConsumer<DirectMessage, TwitterWrapper> directMessageConsumer) {
        onDirectMessageBiConsumersSet.add(directMessageConsumer);
        return this;
    }

    public Kagechiyo onDirectMessage(String command, java.util.function.Consumer<DirectMessage> consumer) {
        onDirectMessageConsumerMap.put(command, consumer);
        return this;
    }

    public Kagechiyo onDirectMessage(String command, java.util.function.BiConsumer<DirectMessage, TwitterWrapper> consumer) {
        onDirectMessageBiConsumerMap.put(command, consumer);
        return this;
    }

    class TwitterWrapper {
        long recipientId;
        Optional<Status> originalStatus;
        Optional<DirectMessage> originalDirectMessage;

        TwitterWrapper(Status status) {
            this.recipientId = status.getUser().getId();
            this.originalStatus = Optional.of(status);
            this.originalDirectMessage = Optional.empty();
        }

        TwitterWrapper(DirectMessage message) {
            this.recipientId = message.getSenderId();
            this.originalStatus = Optional.empty();
            this.originalDirectMessage = Optional.of(message);
        }

        public void updateStatus(String text) {
            try {
                twitter.updateStatus(text);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        public void reply(String text) {
            originalStatus.ifPresent(oritinalStatus -> {
                try {
                    twitter.updateStatus(
                        new StatusUpdate(String.format("@%s %s", oritinalStatus.getUser().getScreenName(),
                            text)).inReplyToStatusId(oritinalStatus.getId()));
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            });
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
        stream.user();
    }

    Listener listener;

    class Listener extends UserStreamAdapter {
        long myTwitterID;
        Listener(Twitter twitter) throws TwitterException {
            this.myTwitterID = twitter.verifyCredentials().getId();
        }
        Listener(long myTwitterID){
            this.myTwitterID = myTwitterID;
        }
        @Override
        public void onStatus(Status status) {
            TwitterWrapper twitterWrapper = new TwitterWrapper(status);
            String[] split = status.getText().split(" ");
            for (UserMentionEntity userMentionEntity : status.getUserMentionEntities()) {
                if ((userMentionEntity.getId() == myTwitterID)) {
                    // call consumers only the status is a mention for the bot account
                    if (split.length >= 2) {
                        String command = split[1];
                        Optional.ofNullable(onMentionConsumerMap.get(command)).ifPresent(e -> e.accept(status));
                        Optional.ofNullable(onMentionBiConsumerMap.get(command)).ifPresent(e -> e.accept(status, twitterWrapper));
                    }
                    onMentionConsumersSet.forEach(e -> e.accept(status));
                    onMentionBiConsumersSet.forEach(e -> e.accept(status, twitterWrapper));
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
                Optional.ofNullable(onDirectMessageConsumerMap.get(command)).ifPresent(e-> e.accept(directMessage));
                Optional.ofNullable(onDirectMessageBiConsumerMap.get(command)).ifPresent(e -> e.accept(directMessage, twitterWrapper));
            }
            onDirectMessageConsumersSet.forEach(e -> e.accept(directMessage));
            onDirectMessageBiConsumersSet.forEach(e -> e.accept(directMessage, twitterWrapper));
        }
    }
}
