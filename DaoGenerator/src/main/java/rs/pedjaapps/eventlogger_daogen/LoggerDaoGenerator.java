/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
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
package rs.pedjaapps.eventlogger_daogen;


import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;

public class LoggerDaoGenerator
{

    public static void main(String[] args) throws Exception
    {
        Schema schema = new Schema(4, "rs.pedjaapps.eventlogger.model");

        addEvents(schema);

        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }

    private static void addEvents(Schema schema)
    {
        Entity event = schema.addEntity("Event");
        event.setHasKeepSections(true);
        event.implementsInterface("Parcelable");
        event.addIdProperty();
        event.addDateProperty("timestamp").notNull();
        event.addStringProperty("short_desc");
        event.addStringProperty("long_desc");
        event.addIntProperty("type").notNull();
        event.addIntProperty("level").notNull();

        Entity icon = schema.addEntity("Icon");
        icon.addIdProperty();
        icon.addByteArrayProperty("icon");

        Property iconIdProperty = event.addLongProperty("icon_id").getProperty();
        event.addToOne(icon, iconIdProperty);
    }

}
