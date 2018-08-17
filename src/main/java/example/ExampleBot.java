package example;

import kagechiyo.Kagechiyo;
import twitter4j.TwitterException;

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
public class ExampleBot {
    public static void main(String[] args) throws TwitterException {
        new Kagechiyo()
            .onMention("echo", (status, twitter) -> twitter.reply(status.getText()))
            .onMention("help", (status, twitter) -> twitter.reply("ググレカス"))
            .onMention("hello", (status, twitter) -> twitter.reply("Hello world!"))
            .start();
    }
}
