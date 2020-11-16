package space.mmty.exception;

public class Thrower {
	public static Message msg(String message) {
		return new Message(message);
	}
	public static Action res(String message) {
		return new Action(message);
	}
}
