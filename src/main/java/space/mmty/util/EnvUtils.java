package space.mmty.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnvUtils extends SysEnv {
	/**
	 * 用于判断是否为pro环境
	 */
	public static boolean isPro() {
		return envVal(true, false);
	}
	/**
	 * 根据环境变量，选定某个值
	 */
	public static <T> T envVal(T onPro, T onTest) {
		return envVal(onPro, onTest, onTest);
	}

	public static <T> T envVal(T onPro, T onTest, T onDev) {
		return SysEnv.envCaller(onPro, onTest, onDev);
	}

	/**
	 * 根据环境变量，以回调的方式，获取某个值
	 */
	public static <T> T envVal(Supplier<T> onPro, Supplier<T> onTest) {
		return envVal(onPro, onTest, onTest);
	}

	public static <T> T envVal(Supplier<T> onPro, Supplier<T> onTest, Supplier<T> onDev) {
		Supplier<T> selected = SysEnv.envCaller(onPro, onTest, onDev);
		return selected.get();
	}

	/**
	 * 根据环境变量，做某件事
	 */
	public static void envToDo(IInvoker onPro, IInvoker onTest) {
		envToDo(onPro, onTest, onTest);
	}

	public static void envToDo(IInvoker onPro, IInvoker onTest, IInvoker onDev) {
		IInvoker selected = SysEnv.envCaller(onPro, onTest, onDev);
		selected.call();
	}

	/**
	 * 根据环境变量，设定某些参数
	 */
	public static <T, R> R envToChange(T start, Consumer<T> onPro, Consumer<T> onTest, Function<T, R> builder) {
		return envToChange(start, onPro, onTest, onTest, builder);
	}

	public static <T, R> R envToChange(T start, Consumer<T> onPro, Consumer<T> onTest, Consumer<T> onDev, Function<T, R> builder) {
		Consumer<T> selected = SysEnv.envCaller(onPro, onTest, onDev);
		selected.accept(start);
		return builder.apply(start);
	}

	/**
	 * 根据环境变量，build一个对象，但是要求是一个Builder设计的对象
	 */
	public static <T, M, R> R envToBuild(T start, Function<T, M> onPro, Function<T, M> onTest, Function<M, R> builder) {
		return envToBuild(start, onPro, onTest, onTest, builder);
	}

	public static <T, M, R> R envToBuild(T start, Function<T, M> onPro, Function<T, M> onTest, Function<T, M> onDev, Function<M, R> builder) {
		Function<T, M> selected = SysEnv.envCaller(onPro, onTest, onDev);
		return builder.apply(selected.apply(start));
	}

	/**
	 * 不同的是，这里个方法的回调，不需要有返回值
	 */
	public static <T, R> void envBuildToDo(T start, Function<T, R> onPro, Function<T, R> onTest, Consumer<R> caller) {
		envBuildToDo(start, onPro, onTest, onTest, caller);
	}

	public static <T, R> void envBuildToDo(T start, Function<T, R> onPro, Function<T, R> onTest, Function<T, R> onDev, Consumer<R> caller) {
		Function<T, R> selected = SysEnv.envCaller(onPro, onTest, onDev);
		caller.accept(selected.apply(start));
	}

	public interface IInvoker {
		void call();
	}
}