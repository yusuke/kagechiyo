package kagechiyo;

import org.junit.Test;
import twitter4j.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
public class KagechiyoTest {

    static final long BOT_USER_ID = 1000L;
    @Test
    public void testOnMention() {
        List<String> commandsExecuted = new ArrayList<>();
        List<String> statusesReceived = new ArrayList<>();

        Kagechiyo kagechiyo = new Kagechiyo(BOT_USER_ID)
            .onMention("echo", status -> commandsExecuted.add("echo"))
            .onMention("hello", status -> commandsExecuted.add("hello"))
            .onMention(status -> statusesReceived.add(status.getText()));
        kagechiyo.listener.onStatus(new StatusMock("@bot echo hello", 1000, BOT_USER_ID));

        assertEquals(1, commandsExecuted.size());
        assertEquals(commandsExecuted.get(0), "echo");

        kagechiyo.listener.onStatus(new StatusMock("@bot hello", 1000, BOT_USER_ID));
        assertEquals(2, commandsExecuted.size());
        assertEquals(commandsExecuted.get(1), "hello");

        // if no command provided, number of executed command is not increased
        kagechiyo.listener.onStatus(new StatusMock("@bot ", 1000, BOT_USER_ID));
        assertEquals(2, commandsExecuted.size());
        assertEquals(3, statusesReceived.size());

        // not mention, disregard
        kagechiyo.listener.onStatus(new StatusMock("hello hello", -1L, -1L));
        assertEquals(2, commandsExecuted.size());
        assertEquals(3, statusesReceived.size());
    }

    @Test
    public void testOnMentionWithWrapper() {
        List<String> commandsExecuted = new ArrayList<>();
        List<String> statusesReceived = new ArrayList<>();

        Kagechiyo kagechiyo = new Kagechiyo(BOT_USER_ID)
            .onMention("echo", (status, wrapper) -> commandsExecuted.add("echo"))
            .onMention("hello", (status, wrapper) -> commandsExecuted.add("hello"))
            .onMention((status, wrapper) -> statusesReceived.add(status.getText()));
        kagechiyo.listener.onStatus(new StatusMock("@bot echo hello", 1000, BOT_USER_ID));
        assertEquals(1, commandsExecuted.size());
        assertEquals(commandsExecuted.get(0), "echo");

        kagechiyo.listener.onStatus(new StatusMock("@bot hello", 1000, BOT_USER_ID));
        assertEquals(2, commandsExecuted.size());
        assertEquals(commandsExecuted.get(1), "hello");

        // if no command provided, disregard
        kagechiyo.listener.onStatus(new StatusMock("@bot ", 1000, BOT_USER_ID));
        assertEquals(2, commandsExecuted.size());
        assertEquals(3, statusesReceived.size());

        // if it's not mention, disregard
        kagechiyo.listener.onStatus(new StatusMock("hello hello", -1L, -1L));
        assertEquals(2, commandsExecuted.size());
        assertEquals(3, statusesReceived.size());

    }

    @Test
    public void testOnDirectMessage() {
        List<String> commandsExecuted = new ArrayList<>();
        List<String> directMessagesReceived = new ArrayList<>();
        Kagechiyo kagechiyo = new Kagechiyo(BOT_USER_ID)
            .onDirectMessage("echo", status -> commandsExecuted.add("echo"))
            .onDirectMessage("hello", status -> commandsExecuted.add("hello"))
            .onDirectMessage(status -> directMessagesReceived.add(status.getText()));

        kagechiyo.listener.onDirectMessage(new DirectMessageMock("echo hello"));
        assertEquals(commandsExecuted.size(), 1);
        assertEquals(commandsExecuted.get(0), "echo");

        kagechiyo.listener.onDirectMessage(new DirectMessageMock("hello hello"));
        assertEquals(commandsExecuted.size(), 2);
        assertEquals(commandsExecuted.get(1), "hello");

        // if no command provided, disregard
        kagechiyo.listener.onDirectMessage(new DirectMessageMock("gler hello "));
        assertEquals(commandsExecuted.size(), 2);

        assertEquals(directMessagesReceived.size(), 3);
    }

    @Test
    public void testOnDirectMessageWithWrapper() {
        List<String> commandsExecuted = new ArrayList<>();
        List<String> directMessagesReceived = new ArrayList<>();
        Kagechiyo kagechiyo = new Kagechiyo(BOT_USER_ID)
            .onDirectMessage("echo", (status, wrapper) -> commandsExecuted.add("echo"))
            .onDirectMessage("hello", (status, wrapper) -> commandsExecuted.add("hello"))
            .onDirectMessage((status, wrapper) -> directMessagesReceived.add(status.getText()));

        kagechiyo.listener.onDirectMessage(new DirectMessageMock("echo hello"));
        assertEquals(commandsExecuted.size(), 1);
        assertEquals(commandsExecuted.get(0), "echo");

        kagechiyo.listener.onDirectMessage(new DirectMessageMock("hello hello"));
        assertEquals(commandsExecuted.size(), 2);
        assertEquals(commandsExecuted.get(1), "hello");

        // if no command provided, disregard
        kagechiyo.listener.onDirectMessage(new DirectMessageMock("gler hello "));
        assertEquals(commandsExecuted.size(), 2);

        assertEquals(directMessagesReceived.size(), 3);
    }
}

class StatusMock implements Status {
    String text;
    long inReplyToStatusId;
    long mentionUserId;

    StatusMock(String text) {
        this.text = text;
        this.inReplyToStatusId = -1L;
    }

    StatusMock(String text, long inReplyToStatusId, long mentionUserId) {
        this.text = text;
        this.inReplyToStatusId = inReplyToStatusId;
        this.mentionUserId = mentionUserId;
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public boolean isTruncated() {
        return false;
    }

    @Override
    public long getInReplyToStatusId() {
        return this.inReplyToStatusId;
    }

    @Override
    public long getInReplyToUserId() {
        return 0;
    }

    @Override
    public String getInReplyToScreenName() {
        return null;
    }

    @Override
    public GeoLocation getGeoLocation() {
        return null;
    }

    @Override
    public Place getPlace() {
        return null;
    }

    @Override
    public boolean isFavorited() {
        return false;
    }

    @Override
    public boolean isRetweeted() {
        return false;
    }

    @Override
    public int getFavoriteCount() {
        return 0;
    }

    @Override
    public User getUser() {
        return new User() {
            @Override
            public long getId() {
                return 1;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getScreenName() {
                return null;
            }

            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public boolean isContributorsEnabled() {
                return false;
            }

            @Override
            public String getProfileImageURL() {
                return null;
            }

            @Override
            public String getBiggerProfileImageURL() {
                return null;
            }

            @Override
            public String getMiniProfileImageURL() {
                return null;
            }

            @Override
            public String getOriginalProfileImageURL() {
                return null;
            }

            @Override
            public String getProfileImageURLHttps() {
                return null;
            }

            @Override
            public String getBiggerProfileImageURLHttps() {
                return null;
            }

            @Override
            public String getMiniProfileImageURLHttps() {
                return null;
            }

            @Override
            public String getOriginalProfileImageURLHttps() {
                return null;
            }

            @Override
            public boolean isDefaultProfileImage() {
                return false;
            }

            @Override
            public String getURL() {
                return null;
            }

            @Override
            public boolean isProtected() {
                return false;
            }

            @Override
            public int getFollowersCount() {
                return 0;
            }

            @Override
            public Status getStatus() {
                return null;
            }

            @Override
            public String getProfileBackgroundColor() {
                return null;
            }

            @Override
            public String getProfileTextColor() {
                return null;
            }

            @Override
            public String getProfileLinkColor() {
                return null;
            }

            @Override
            public String getProfileSidebarFillColor() {
                return null;
            }

            @Override
            public String getProfileSidebarBorderColor() {
                return null;
            }

            @Override
            public boolean isProfileUseBackgroundImage() {
                return false;
            }

            @Override
            public boolean isDefaultProfile() {
                return false;
            }

            @Override
            public boolean isShowAllInlineMedia() {
                return false;
            }

            @Override
            public int getFriendsCount() {
                return 0;
            }

            @Override
            public Date getCreatedAt() {
                return null;
            }

            @Override
            public int getFavouritesCount() {
                return 0;
            }

            @Override
            public int getUtcOffset() {
                return 0;
            }

            @Override
            public String getTimeZone() {
                return null;
            }

            @Override
            public String getProfileBackgroundImageURL() {
                return null;
            }

            @Override
            public String getProfileBackgroundImageUrlHttps() {
                return null;
            }

            @Override
            public String getProfileBannerURL() {
                return null;
            }

            @Override
            public String getProfileBannerRetinaURL() {
                return null;
            }

            @Override
            public String getProfileBannerIPadURL() {
                return null;
            }

            @Override
            public String getProfileBannerIPadRetinaURL() {
                return null;
            }

            @Override
            public String getProfileBannerMobileURL() {
                return null;
            }

            @Override
            public String getProfileBannerMobileRetinaURL() {
                return null;
            }

            @Override
            public boolean isProfileBackgroundTiled() {
                return false;
            }

            @Override
            public String getLang() {
                return null;
            }

            @Override
            public int getStatusesCount() {
                return 0;
            }

            @Override
            public boolean isGeoEnabled() {
                return false;
            }

            @Override
            public boolean isVerified() {
                return false;
            }

            @Override
            public boolean isTranslator() {
                return false;
            }

            @Override
            public int getListedCount() {
                return 0;
            }

            @Override
            public boolean isFollowRequestSent() {
                return false;
            }

            @Override
            public URLEntity[] getDescriptionURLEntities() {
                return new URLEntity[0];
            }

            @Override
            public URLEntity getURLEntity() {
                return null;
            }

            @Override
            public int compareTo(User o) {
                return 0;
            }

            @Override
            public RateLimitStatus getRateLimitStatus() {
                return null;
            }

            @Override
            public int getAccessLevel() {
                return 0;
            }
        };
    }

    @Override
    public boolean isRetweet() {
        return false;
    }

    @Override
    public Status getRetweetedStatus() {
        return null;
    }

    @Override
    public long[] getContributors() {
        return new long[0];
    }

    @Override
    public int getRetweetCount() {
        return 0;
    }

    @Override
    public boolean isRetweetedByMe() {
        return false;
    }

    @Override
    public long getCurrentUserRetweetId() {
        return 0;
    }

    @Override
    public boolean isPossiblySensitive() {
        return false;
    }

    @Override
    public String getLang() {
        return null;
    }

    @Override
    public Scopes getScopes() {
        return null;
    }

    @Override
    public int compareTo(Status o) {
        return 0;
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        UserMentionEntity ume = new UserMentionEntity(){

            @Override
            public String getText() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getScreenName() {
                return null;
            }

            @Override
            public long getId() {
                return mentionUserId;
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 0;
            }
        };
        UserMentionEntity[] umes = new UserMentionEntity[1];
        umes[0] = ume;
        return umes;
    }

    @Override
    public URLEntity[] getURLEntities() {
        return new URLEntity[0];
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        return new HashtagEntity[0];
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        return new MediaEntity[0];
    }

    @Override
    public MediaEntity[] getExtendedMediaEntities() {
        return new MediaEntity[0];
    }

    @Override
    public SymbolEntity[] getSymbolEntities() {
        return new SymbolEntity[0];
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }
}

class DirectMessageMock implements DirectMessage {
    String text;

    DirectMessageMock(String text) {
        this.text = text;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getSenderId() {
        return 0;
    }

    @Override
    public long getRecipientId() {
        return 0;
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public String getSenderScreenName() {
        return null;
    }

    @Override
    public String getRecipientScreenName() {
        return null;
    }

    @Override
    public User getSender() {
        return null;
    }

    @Override
    public User getRecipient() {
        return null;
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        return new UserMentionEntity[0];
    }

    @Override
    public URLEntity[] getURLEntities() {
        return new URLEntity[0];
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        return new HashtagEntity[0];
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        return new MediaEntity[0];
    }

    @Override
    public MediaEntity[] getExtendedMediaEntities() {
        return new MediaEntity[0];
    }

    @Override
    public SymbolEntity[] getSymbolEntities() {
        return new SymbolEntity[0];
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }
}