package com.estradahermanos.shoestock.utilities;

public enum ShoeType
{
    DAMA("Dama"),
    CABALLERO("Caballero"),
    NINO("Niño"),
    NINA("Niña");

    private final String label;

    ShoeType(String label)
    {
        this.label = label;
    }

    public static boolean isValid(String value)
    {
        for (ShoeType type : values())
        {
            if (type.label.equals(value))
            {
                return true;
            }
        }
        return false;
    }
}
