package com.zxt.dlna.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Application;

public class ThreadUtils {
	/** 创建线程池 */
	private static ExecutorService executor = null;

	public synchronized static void prepare() {
		if (executor == null || executor.isShutdown()) {
			executor = Executors.newFixedThreadPool(5);
		}
	}

	/**
	 * 在{@link Application#onTerminate}的时候销毁线程池
	 * */
	public synchronized static void shutdown() {
		if (executor != null) {
			if (!executor.isShutdown()) {
				executor.shutdown();
			}
			executor = null;
		}
	}

	/**
	 * 执行Runnable形式的任务
	 * 
	 * @param task
	 *            需要执行的任务
	 * */
	public static void execute(Runnable task) {
		executor.execute(task);
	}

	/**
	 * 执行Callable形式的任务
	 * 
	 * @param task
	 *            需要执行的任务
	 * @return 任务执行结束返回结果信息
	 * */
	public static Future<?> submit(Callable<?> task) {
		return executor.submit(task);
	}

}
