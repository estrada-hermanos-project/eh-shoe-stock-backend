package com.estradahermanos.shoestock.utilities;

public enum OrderStatusEnum
{
    PENDIENTE,
    RECIBIDA;

    public static boolean isValid(String value)
    {
        if (value == null)
        {
            return false;
        }
        for (OrderStatusEnum status : values())
        {
            if (status.name().equals(value))
            {
                return true;
            }
        }
        return false;
    }
}
