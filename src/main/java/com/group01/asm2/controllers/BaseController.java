package com.group01.asm2.controllers;

import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.security.AppActionGuard;
import com.group01.asm2.security.RateLimitPolicy;

import java.util.function.Supplier;

public abstract class BaseController {
    protected void guarded(Runnable action) {
        try {
            AppActionGuard.runGlobal(action);
        } catch (AppException exception) {
            handleAppException(exception);
        } catch (Exception exception) {
            exception.printStackTrace();
            handleUnexpectedException();
        }
    }

    protected <T> T guardedCall(Supplier<T> action) {
        try {
            return AppActionGuard.callGlobal(action);
        } catch (AppException exception) {
            handleAppException(exception);
            return null;
        } catch (Exception exception) {
            exception.printStackTrace();
            handleUnexpectedException();
            return null;
        }
    }

    protected void guarded(
        RateLimitPolicy policy,
        String identity,
        Runnable action
    ) {
        try {
            AppActionGuard.runLimited(policy, identity, action);
        } catch (AppException exception) {
            handleAppException(exception);
        } catch (Exception exception) {
            exception.printStackTrace();
            handleUnexpectedException();
        }
    }

    protected <T> T guardedCall(
        RateLimitPolicy policy,
        String identity,
        Supplier<T> action
    ) {
        try {
            return AppActionGuard.callLimited(policy, identity, action);
        } catch (AppException exception) {
            handleAppException(exception);
            return null;
        } catch (Exception exception) {
            exception.printStackTrace();
            handleUnexpectedException();
            return null;
        }
    }

    protected abstract void handleAppException(AppException exception);

    protected void handleUnexpectedException() {
        System.out.println("Something went wrong. Please try again.");
    }
}