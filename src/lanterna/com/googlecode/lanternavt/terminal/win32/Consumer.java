package com.googlecode.lanternavt.terminal.win32;

public abstract class Consumer<T>
{
    abstract void accept(T t);
    private Consumer<? super T> after;
    
    public Consumer<? super T> getAfter()
    {
    	return after;
    }
    
    public Consumer<T> andThen(Consumer<? super T> after)
    {
        if (after != null)
        {
        	this.after = after;
        }
        return this;
    }
}