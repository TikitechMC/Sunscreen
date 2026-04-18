package me.combimagnetron.sunscreen.neo.input.base;

public interface Action<T> {

    T context();

    class ClickAction implements Action<Object> {

        @Override
        public Object context() {
            return null;
        }
    }

    class DragAction implements Action<Object> {

        @Override
        public Object context() {
            return null;
        }
    }

    class HoverAction implements Action<Object> {

        @Override
        public Object context() {
            return null;
        }
    }

}
