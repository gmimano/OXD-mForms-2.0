package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.fcitmuk.db.util.Persistent;

/**
 * A condition which is part of a rule. For definition of a rule, go to the Rule
 * class. E.g. If sex is Male. If age is greater than than 4. etc
 * 
 * @author Daniel Kayiwa
 */
public class Condition implements Persistent {

	/** expression functions **/
	private static final int FUNC_SUM = 1;
	private static final int FUNC_MAX = 2;
	private static final int FUNC_MIN = 3;
	private static final int FUNC_AVG = 4;

	/** The unique identifier of the question referenced by this condition. */
	private short questionId = EpihandyConstants.NULL_ID;

	/** The operator of the condition. Eg Equal to, Greater than, etc. */
	private byte operator = EpihandyConstants.OPERATOR_NULL;

	/** The aggregate function. Eg Length, Value. */
	private byte function = EpihandyConstants.FUNCTION_VALUE;

	/**
	 * The value checked to see if the condition is true or false. For the above
	 * example, the value would be 4 or the id of the Male option. For a list of
	 * options this value is the option id, not the value or text value.
	 */
	private String value = EpihandyConstants.EMPTY_STRING;

	private String secondValue = EpihandyConstants.EMPTY_STRING;

	/** The unique identifier of a condition. */
	private short id = EpihandyConstants.NULL_ID;

	/** Creates a new condition object. */
	public Condition() {

	}

	/** Copy constructor. */
	public Condition(Condition condition) {
		this(condition.getId(), condition.getQuestionId(), condition.getOperator(), condition
				.getFunction(), condition.getValue());
	}

	/**
	 * Creates a new condition object from its parameters.
	 * 
	 * @param id
	 *            - the numeric identifier of the condition.
	 * @param questionId
	 *            - the numeric identifier of the question.
	 * @param operator
	 *            - the condition operator.
	 * @param value
	 *            - the value to be equated to.
	 */
	public Condition(short id, short questionId, byte operator, byte function, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setFunction(function);
		setValue(value);
		setId(id);
	}

	public byte getOperator() {
		return operator;
	}

	public void setOperator(byte operator) {
		this.operator = operator;
	}

	public short getQuestionId() {
		return questionId;
	}

	public void setQuestionId(short questionId) {
		this.questionId = questionId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public short getId() {
		return id;
	}

	public void setId(short conditionId) {
		this.id = conditionId;
	}

	public byte getFunction() {
		return function;
	}

	public void setFunction(byte function) {
		this.function = function;
	}

	public String getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	public boolean isTrue(RepeatQtnsData data, boolean validation) {
		boolean ret = true;
		String tempValue = value;

		try {
			QuestionData qn = data.getQuestionByDefId(this.questionId);
			if (qn != null) {
				String realValue = getRealValue(data);
				if (realValue == null || realValue.trim().length() == 0) {
					return (qn.getAnswer() == null || qn.getValueAnswer().trim().length() == 0);
				}

				value = realValue;
				ret = isTrue(qn, validation);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		value = tempValue;
		return ret;
	}

	private String getRealValue(RepeatQtnsData data) {
		String rootNode = data.getDef().getQtnDef().getVariableName();
		int expressionFunction = getAggregateFunction();
		if (value.startsWith(rootNode + "/")) {
			QuestionData qn2 = data.getQuestion("/" + value);
			if (qn2 != null) {
				return qn2.getValueAnswer();
			} else {
				return null;
			}
		} else if (expressionFunction > 0) {
			int lastIndexOf = value.lastIndexOf(')');
			if (lastIndexOf < 0) {
				lastIndexOf = value.length();
			}
			String expression = value.substring(4, lastIndexOf);
			int indexOf = 0;
			double answer = expressionFunction == FUNC_MIN ? Double.MAX_VALUE : 0d;
			int count = 0;
			while (indexOf >= 0) {
				int indexOf2 = expression.indexOf('|', indexOf + 1);
				String expressionArgRef = expression
						.substring(indexOf, indexOf2 > 0 ? indexOf2 : expression.length()).trim()
						.intern();
				String expressionArg = expressionArgRef;
				if (expressionArgRef.startsWith(rootNode + "/")) {
					QuestionData qn2 = data.getQuestion("/" + expressionArgRef);
					if (qn2 != null) {
						expressionArg = qn2.getValueAnswer();
					}
				}
				if (expressionArg != null && expressionArg.trim().length() > 0) {
					try {
						double argVal = Double.parseDouble(expressionArg);
						answer = processAggregate(answer, argVal, expressionFunction);
					} catch (NumberFormatException e) {
						return null; // unable to sum values
					}
				}
				indexOf = indexOf2 >= 0 ? indexOf2 + 1 : indexOf2;
				count++;
			}
			if (expressionFunction == FUNC_AVG) {
				answer = answer / count;
			}
			return String.valueOf(answer);
		}
		return value;
	}

	/**
	 * Test if a condition is true or false.
	 */
	public boolean isTrue(FormData data, boolean validation) {
		String tempValue = value;
		boolean ret = true;

		try {
			QuestionData qn = data.getQuestion(this.questionId);

			if (qn != null) {
				if (isBindExpression(data)) {
					QuestionData qn2 = data.getQuestion("/" + value);
					if (qn2 != null) {
						value = qn2.getValueAnswer();
						if (value == null || value.trim().length() == 0) {
							value = tempValue;
							if (qn2.getAnswer() == null
									|| qn2.getValueAnswer().trim().length() == 0)
								return true; // Both questions not answered yet
							return false;
						} else if (qn.getAnswer() == null
								|| qn.getValueAnswer().trim().length() == 0) {
							if (qn.getDef().getType() != QuestionDef.QTN_TYPE_REPEAT) {
								value = tempValue;
								return false;
							}
						}
					}
				} else if (isAggregateFunction()) {
					String realValue = getRealValue(data);
					if (realValue == null || realValue.trim().length() == 0) {
						return (qn.getAnswer() == null || qn.getValueAnswer()
								.trim().length() == 0);
					} else if (qn.getAnswer() == null
							|| qn.getValueAnswer().trim().length() == 0) {
						if (qn.getDef().getType() != QuestionDef.QTN_TYPE_REPEAT) {
							return false;
						}
					}
					value = realValue;
				}

				ret = isTrue(qn, validation);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		value = tempValue;

		return ret;
	}

	private boolean isTrue(QuestionData qn, boolean validation) {
		boolean ret = true;
		switch (qn.getDef().getType()) {
		case QuestionDef.QTN_TYPE_TEXT:
			ret = isTextTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_REPEAT:
		case QuestionDef.QTN_TYPE_NUMERIC:
		case QuestionDef.QTN_TYPE_PHONENUMBER:
			ret = isNumericTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_DATE:
			ret = isDateTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_DATE_TIME:
			ret = isDateTimeTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_DECIMAL:
			ret = isDecimalTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			ret = isListExclusiveTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			ret = isListMultipleTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_TIME:
			ret = isTimeTrue(qn, validation);
			break;
		case QuestionDef.QTN_TYPE_BOOLEAN:
			ret = isTextTrue(qn, validation);
			break;
		}
		return ret;
	}

	/**
	 * Attempt to convert the condition expression into a real value. Supported
	 * features:
	 * <ul>
	 * <li>Fetch the value of a referenced node e.g. root/question1
	 * <li>Sum the values of various nodes e.g. sum(root/q1 | root/q2 | root/q3)
	 * <li>Avg the values of various nodes e.g. avg(root/q1 | root/q2 | root/q3)
	 * <li>Min the values of various nodes e.g. min(root/q1 | root/q2 | root/q3)
	 * <li>Max the values of various nodes e.g. max(root/q1 | root/q2 | root/q3)
	 * 
	 * @param data
	 *            the form data containing the current forms data
	 * @return String representing the real value of the expression
	 */
	private String getRealValue(FormData data) {
		String rootNode = data.getDef().getVariableName();
		int expressionFunction = getAggregateFunction();
		if (isAggregateFunction()) {
			int lastIndexOf = value.lastIndexOf(')');
			if (lastIndexOf < 0) {
				lastIndexOf = value.length();
			}
			String expression = value.substring(4, lastIndexOf);
			int indexOf = 0;
			double answer = expressionFunction == FUNC_MIN ? Double.MAX_VALUE : 0d;
			int count = 0;
			while (indexOf >= 0) {
				int indexOf2 = expression.indexOf('|', indexOf + 1);
				String expressionArgRef = expression
						.substring(indexOf, indexOf2 > 0 ? indexOf2 : expression.length()).trim()
						.intern();
				String expressionArg = expressionArgRef;
				if (expressionArgRef.startsWith(rootNode + "/")) {
					QuestionData qn2 = data.getQuestion("/" + expressionArgRef);
					if (qn2 != null) {
						expressionArg = qn2.getValueAnswer();
					}
				}
				if (expressionArg != null && expressionArg.trim().length() > 0) {
					try {
						double argVal = Double.parseDouble(expressionArg);
						answer = processAggregate(answer, argVal, expressionFunction);
					} catch (NumberFormatException e) {
						return null; // unable to sum values
					}
				}
				indexOf = indexOf2 >= 0 ? indexOf2 + 1 : indexOf2;
				count++;
			}
			if (expressionFunction == FUNC_AVG) {
				answer = answer / count;
			}
			return String.valueOf(answer);
		}
		return value;
	}

	private boolean isBindExpression(FormData data) {
		return value.startsWith(data.getDef().getVariableName() + "/");
	}
	
	private boolean isAggregateFunction() {
		return getAggregateFunction() > 0;
	}
	
	private int getAggregateFunction() {
		if (value.startsWith("sum(")) {
			return FUNC_SUM;
		} else if (value.startsWith("avg(")) {
			return FUNC_AVG;
		} else if (value.startsWith("min(")) {
			return FUNC_MIN;
		} else if (value.startsWith("max(")) {
			return FUNC_MAX;
		}
		return -1;
	}

	private double processAggregate(double answer, double nextArg, int function) {
		switch (function) {
		case (FUNC_SUM):
		case (FUNC_AVG):
			return answer + nextArg;
		case (FUNC_MAX):
			return answer > nextArg ? answer : nextArg;
		case (FUNC_MIN):
			return answer < nextArg ? answer : nextArg;
		}
		return 0;
	}

	private boolean isNumericTrue(QuestionData data, boolean validation) {
		return isDecimalTrue(data, validation);
	}

	// TODO Should this test be case sensitive?
	private boolean isTextTrue(QuestionData data, boolean validation) {
		Object answer = data.getValueAnswer();

		if (function == EpihandyConstants.FUNCTION_VALUE) {
			if (answer == null || answer.toString().trim().length() == 0) {
				if (validation || operator == EpihandyConstants.OPERATOR_NOT_EQUAL
						|| operator == EpihandyConstants.OPERATOR_NOT_START_WITH
						|| operator == EpihandyConstants.OPERATOR_NOT_CONTAIN)
					return true;

				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			}

			if (operator == EpihandyConstants.OPERATOR_EQUAL)
				return value.equals(data.getValueAnswer());
			else if (operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
				return !value.equals(data.getValueAnswer());
			else if (operator == EpihandyConstants.OPERATOR_STARTS_WITH)
				return answer.toString().startsWith(value);
			else if (operator == EpihandyConstants.OPERATOR_NOT_START_WITH)
				return !answer.toString().startsWith(value);
			else if (operator == EpihandyConstants.OPERATOR_CONTAINS)
				return answer.toString().indexOf(value) >= 0;
			else if (operator == EpihandyConstants.OPERATOR_NOT_CONTAIN)
				return !(answer.toString().indexOf(value) >= 0);
		} else {
			if (answer == null || answer.toString().trim().length() == 0)
				return true;

			long len1 = 0, len2 = 0, len = 0;
			if (value != null && value.trim().length() > 0)
				len1 = Long.parseLong(value);
			if (secondValue != null && secondValue.trim().length() > 0)
				len2 = Long.parseLong(secondValue);

			len = answer.toString().trim().length();

			if (operator == EpihandyConstants.OPERATOR_EQUAL)
				return len == len1;
			else if (operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
				return len != len1;
			else if (operator == EpihandyConstants.OPERATOR_LESS)
				return len < len1;
			else if (operator == EpihandyConstants.OPERATOR_LESS_EQUAL)
				return len <= len1;
			else if (operator == EpihandyConstants.OPERATOR_GREATER)
				return len > len1;
			else if (operator == EpihandyConstants.OPERATOR_GREATER_EQUAL)
				return len >= len1;
			else if (operator == EpihandyConstants.OPERATOR_BETWEEN)
				return len > len1 && len < len2;
			else if (operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
				return !(len > len1 && len < len2);
		}

		return false;
	}

	/**
	 * Tests if the passed parameter date value is equal to the value of the
	 * condition.
	 * 
	 * @param data
	 *            - passed parameter date value.
	 * @return - true when the two values are the same, else false.
	 */
	private boolean isDateTrue(QuestionData qtn, boolean validation) {
		try {
			if (qtn.getAnswer() == null || qtn.getAnswer().toString().trim().length() == 0) {
				if (validation || operator == EpihandyConstants.OPERATOR_NOT_EQUAL
						|| operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			}

			if (!(qtn.getAnswer() instanceof Date)
					&& qtn.getAnswer().equals(qtn.getDef().getDefaultValue()))
				return (validation ? true : false);

			TimeZone timeZone = java.util.TimeZone.getDefault();
			/*
			 * java.util. TimeZone .getTimeZone ("GMT")
			 */
			Calendar calenderAnswer = Calendar.getInstance(timeZone); // "GMT"//+830
			calenderAnswer.setTime((Date) qtn.getAnswer());

			/*
			 * java.util. TimeZone . getTimeZone ("GMT")
			 */
			Calendar calenderdateValue = Calendar.getInstance(timeZone); // "GMT"//+830
			if (isDateFunction(value))
				calenderdateValue.setTime(getDateFunctionValue(value));
			else
				calenderdateValue.setTime(fromString2Date(value));

			/*
			 * java . util . TimeZone . getTimeZone ( "GMT" )
			 */
			Calendar calenderdateSecondDateValue = Calendar.getInstance(timeZone); // "GMT"//+830
			if (secondValue != null && secondValue.trim().length() > 0) {
				if (isDateFunction(secondValue))
					calenderdateSecondDateValue.setTime(getDateFunctionValue(secondValue));
				else
					calenderdateSecondDateValue.setTime(fromString2Date(secondValue));
			}

			if (operator == EpihandyConstants.OPERATOR_EQUAL)
				return calenderdateValue.equals(calenderAnswer);
			else if (operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
				return !calenderdateValue.equals(calenderAnswer);
			else if (operator == EpihandyConstants.OPERATOR_LESS)
				return calenderAnswer.before(calenderdateValue);
			else if (operator == EpihandyConstants.OPERATOR_LESS_EQUAL)
				return calenderAnswer.before(calenderdateValue)
						|| calenderdateValue.equals(calenderAnswer);
			else if (operator == EpihandyConstants.OPERATOR_GREATER)
				return calenderAnswer.after(calenderdateValue);
			else if (operator == EpihandyConstants.OPERATOR_GREATER_EQUAL)
				return calenderAnswer.after(calenderdateValue)
						|| calenderdateValue.equals(calenderAnswer);
			else if (operator == EpihandyConstants.OPERATOR_BETWEEN)
				return calenderAnswer.after(calenderdateValue)
						&& calenderdateValue.before(calenderdateSecondDateValue);
			else if (operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
				return !(calenderAnswer.after(calenderdateValue) && calenderdateValue
						.before(calenderdateSecondDateValue));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	private boolean isDateFunction(String value) {
		if (value == null)
			return false;

		return (value.toLowerCase().equals("'now()'") || value.toLowerCase().equals("'date()'")
				|| value.toLowerCase().equals("'getdate()'") || value.toLowerCase().equals(
				"'today()'"));
	}

	private Date getDateFunctionValue(String function) {
		return new Date();
	}

	private Date fromString2Date(String value) {
		return new Date(); // TODO needs to parse this and create proper date;
	}

	private boolean isDateTimeTrue(QuestionData data, boolean validation) {
		return isDateTrue(data, validation);// value.equals(data.getTextAnswer());
	}

	private boolean isTimeTrue(QuestionData data, boolean validation) {
		return value.equals(data.getTextAnswer());
	}

	private boolean isListMultipleTrue(QuestionData data, boolean validation) {
		try {
			if (data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0) {
				if (validation || operator == EpihandyConstants.OPERATOR_NOT_EQUAL
						|| operator == EpihandyConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			}

			switch (operator) {
			case EpihandyConstants.OPERATOR_EQUAL:
				return data.getValueAnswer().toString().indexOf(value) >= 0;// data.getValueAnswer().equals(value);
			case EpihandyConstants.OPERATOR_NOT_EQUAL:
				return !(data.getValueAnswer().indexOf(value) >= 0);// !data.getValueAnswer().equals(value);
			case EpihandyConstants.OPERATOR_IN_LIST:
				return value.indexOf(data.getValueAnswer()) >= 0;
			case EpihandyConstants.OPERATOR_NOT_IN_LIST:
				return !(value.indexOf(data.getValueAnswer()) >= 0);
			default:
				return false;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return false;
	}

	private boolean isListExclusiveTrue(QuestionData data, boolean validation) {
		try {
			if (data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0) {
				if (validation || operator == EpihandyConstants.OPERATOR_NOT_EQUAL
						|| operator == EpihandyConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			}

			switch (operator) {
			case EpihandyConstants.OPERATOR_EQUAL:
				return data.getValueAnswer().equals(value);
			case EpihandyConstants.OPERATOR_NOT_EQUAL:
				return !data.getValueAnswer().equals(value);
			case EpihandyConstants.OPERATOR_IN_LIST:
				return value.indexOf(data.getValueAnswer()) > 0;
			case EpihandyConstants.OPERATOR_NOT_IN_LIST:
				return !(value.indexOf(data.getValueAnswer()) > 0);
			default:
				return false;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		return false;
	}

	private boolean isDecimalTrue(QuestionData data, boolean validation) {

		try {
			if (data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0) {
				if (validation || operator == EpihandyConstants.OPERATOR_NOT_EQUAL
						|| operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			} else if (operator == EpihandyConstants.OPERATOR_IS_NOT_NULL)
				return true;

			double answer = Double.parseDouble(data.getValueAnswer());
			double floatValue = Double.parseDouble(value);

			double secondFloatValue = floatValue;
			if (secondValue != null && secondValue.trim().length() > 0)
				secondFloatValue = Double.parseDouble(secondValue);

			if (operator == EpihandyConstants.OPERATOR_EQUAL)
				return floatValue == answer;
			else if (operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
				return floatValue != answer;
			else if (operator == EpihandyConstants.OPERATOR_LESS)
				return answer < floatValue;
			else if (operator == EpihandyConstants.OPERATOR_LESS_EQUAL)
				return answer < floatValue || floatValue == answer;
			else if (operator == EpihandyConstants.OPERATOR_GREATER)
				return answer > floatValue;
			else if (operator == EpihandyConstants.OPERATOR_GREATER_EQUAL)
				return answer > floatValue || floatValue == answer;
			else if (operator == EpihandyConstants.OPERATOR_BETWEEN)
				return answer > floatValue && floatValue < secondFloatValue;
			else if (operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > floatValue && floatValue < secondFloatValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	/**
	 * Reads the condition object from the supplied stream.
	 * 
	 * @param dis
	 *            - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException,
			IllegalAccessException {
		setId(dis.readShort());
		setQuestionId(dis.readShort());
		setOperator(dis.readByte());
		setValue(dis.readUTF().intern());
		setFunction(dis.readByte());
	}

	/**
	 * Writes the Condition object to the supplied stream.
	 * 
	 * @param dos
	 *            - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getId());
		dos.writeShort(getQuestionId());
		dos.writeByte(getOperator());
		dos.writeUTF(getValue());
		dos.writeByte(getFunction());
	}

	public String getValue(FormData data) {
		if (value.startsWith(data.getDef().getVariableName() + "/")) {
			QuestionData qn = data.getQuestion("/" + value);
			if (qn != null)
				return qn.getValueAnswer();
		}
		return value;
	}
}