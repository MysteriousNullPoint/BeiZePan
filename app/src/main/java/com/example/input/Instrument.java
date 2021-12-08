package com.example.input;

public class Instrument {
    private int id;
    private String name;
    private int count;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the epc
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the epc to set
     */
    public void setEpc(String name) {
        this.name = name;
    }
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Instrument [id=" + id + ", name=" + name + ", count=" + count + "]";
    }


}

