/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.q.actions.questions;

import info.faceland.q.actions.options.InvalidOptionException;

import java.util.*;

public class QuestionManager {

    private Map<UUID, LinkedList<AbstractQuestion>> questionMap;
    private static int questionId;

    public QuestionManager() {
        questionMap = new HashMap<>();
    }

    public static int getNextQuestionId() {
        return questionId++;
    }

    public void appendQuestion(Question question) throws IllegalArgumentException {
        if (question.getOptions().size() == 0) {
            throw new IllegalArgumentException("there must be at least one option");
        }

        LinkedList<AbstractQuestion> questions = questionMap.get(question.getTarget());
        if (questions == null) {
            questions = new LinkedList<>();
        }
        questions.add(question);
        questionMap.put(question.getTarget(), questions);
    }

    public void appendLinkedQuestion(LinkedQuestion question) throws IllegalArgumentException {
        if (question.getOptions().size() == 0) {
            throw new IllegalArgumentException("there must be at least one option");
        }

        for (UUID target : question.getTargets()) {
            LinkedList<AbstractQuestion> questions = questionMap.get(target);
            if (questions == null) {
                questions = new LinkedList<>();
            }
            questions.add(question);
            questionMap.put(target, questions);
        }
    }

    public LinkedList<AbstractQuestion> getQuestions(UUID target) {
        LinkedList<AbstractQuestion> questions = questionMap.get(target);
        if (questions == null) {
            questions = new LinkedList<>();
        }
        return questions;
    }

    public AbstractQuestion peekAtFirstQuestion(UUID target) throws IllegalStateException {
        LinkedList<AbstractQuestion> questions = getQuestions(target);
        if (questions.size() == 0) {
            removeAllQuestions(target);
            throw new IllegalStateException("target must have some questions");
        }
        return questions.peek();
    }

    public void removeAllQuestions(UUID target) {
        questionMap.remove(target);
    }

    public Runnable answerFirstQuestion(UUID target, String command) throws IllegalStateException,
                                                                            InvalidOptionException {
        return peekAtFirstQuestion(target).getOption(command).getReaction();
    }

    public void removeFirstQuestion(UUID target) throws Exception {
        LinkedList<AbstractQuestion> playersActiveQuestions = getQuestions(target);
        if (playersActiveQuestions.size() == 0) {
            removeAllQuestions(target);
            throw new Exception("There are no pending questions");
        }
        if (playersActiveQuestions.peek() instanceof LinkedQuestion) {
            LinkedQuestion question = (LinkedQuestion)playersActiveQuestions.peek();
            int id = question.id;
            for (UUID qTarget : new ArrayList<>(question.getTargets()))
                removeQuestionId(qTarget, id);
        } else {
            playersActiveQuestions.removeFirst();
        }
    }

    public void removeQuestionInQueue(UUID target, int queueNumber) throws Exception {
        LinkedList<AbstractQuestion> playersActiveQuestions = getQuestions(target);
        if (playersActiveQuestions.size() == 0) {
            removeAllQuestions(target);
            throw new Exception("There are no pending questions");
        }
        try {
            if (playersActiveQuestions.get(queueNumber) instanceof LinkedQuestion) {
                LinkedQuestion question = (LinkedQuestion)playersActiveQuestions.get(queueNumber);
                int id = question.id;
                for (UUID qTarget : new ArrayList<>(question.getTargets()))
                    removeQuestionId(qTarget, id);
            } else
                playersActiveQuestions.removeFirst();
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Invalid question id.");
        }
    }

    public void removeQuestionId(UUID target, int id) throws Exception {
        LinkedList<AbstractQuestion> playersActiveQuestions = getQuestions(target);
        for (AbstractQuestion question : new LinkedList<>(playersActiveQuestions)) {
            if (question.id == id) {
                playersActiveQuestions.remove(question);
            }
        }
    }

    public boolean hasQuestion(UUID target) {
        try {
            LinkedList<AbstractQuestion> playersActiveQuestions = getQuestions(target);
            if (playersActiveQuestions.size() == 0) {
                removeAllQuestions(target);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

}
