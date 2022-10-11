package com.ca.classattender;

import org.jetbrains.annotations.NotNull;

public class SlotModel {
    public int slotTemplate;
    public String subName, slotTime, subCode, subTeacher, subDay;

    public SlotModel(@NotNull int slotTemplate, String subName, String slotTime, String subCode, String subTeacher, String subDay){
        this.slotTemplate = slotTemplate;
        this.subName = subName;
        this.slotTime = slotTime;
        this.subCode = subCode;
        this.subTeacher = subTeacher;
        this.subDay = subDay;
    }
}
