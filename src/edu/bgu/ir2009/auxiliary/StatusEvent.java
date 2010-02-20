package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 20/02/2010
 * Time: 22:35:13
 */
public class StatusEvent {
    public enum Type {
        MSG,
        DONE
    }

    private final Type type;
    private final String msg;

    public StatusEvent() {
        type = Type.DONE;
        msg = "Indexing finished successfully!";
    }

    public StatusEvent(String msg) {
        type = Type.MSG;
        this.msg = msg;
    }

    public Type getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
