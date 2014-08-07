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

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class LoggerDaoGenerator
{

    public static void main(String[] args) throws Exception
    {
        Schema schema = new Schema(3, "rs.pedjaapps.eventlogger.model");

        addEvents(schema);

        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, "src-gen");
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
        event.addByteArrayProperty("icon");

        /*Entity question = schema.addEntity("Question");
        question.addLongProperty("question_id").primaryKey();
        question.addIntProperty("question_order");
        question.addBooleanProperty("searchable");
        question.addStringProperty("type");
        question.addIntProperty("value_min");
        question.addIntProperty("value_max");
        question.addBooleanProperty("required");
        question.addBooleanProperty("sortable");
        question.addBooleanProperty("multiselect");
        question.addStringProperty("question_name");
        question.addStringProperty("question_name_profile");
        question.addStringProperty("question_name_code");
        question.addStringProperty("hide_from");
        question.addStringProperty("description");

        Property groupId = question.addLongProperty("group_id").notNull().getProperty();
        ToMany groupToQuestion = event.addToMany(question, groupId);
        groupToQuestion.setName("questions");

        Entity answer = schema.addEntity("Answer");
        answer.addIdProperty();
        answer.addStringProperty("text");

        Property questionId = answer.addLongProperty("question_id").notNull().getProperty();
        ToMany questionToAnswer = question.addToMany(answer, questionId);
        questionToAnswer.setName("answers");*/
    }

}
