/*
 * MIT License
 *
 * Copyright (c) 2017 Inova IT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package si.inova.neatle.operation;

/**
 * Observes the status of an operation.
 */
public interface OperationObserver {

    /**
     * Invoked when a command has started execution.
     *
     * @param op      the operation
     * @param command the command
     */
    void onCommandStarted(Operation op, Command command);

    /**
     * Invoked when a command has finished successfully.
     *
     * @param op      the operation
     * @param command the command
     * @param result  the result of the command
     */
    void onCommandSuccess(Operation op, Command command, CommandResult result);

    /**
     * Invoked when a command has finished unsuccessfully.
     *
     * @param operation the operation
     * @param command   the command
     * @param error     the error code
     */
    void onCommandError(Operation operation, Command command, int error);

    /**
     * Invoked when an operation has finished either successfully or unsuccessfully.
     *
     * @param op      the operation
     * @param results the result of all the operations
     */
    void onOperationFinished(Operation op, OperationResults results);
}
