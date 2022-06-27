package uz.smd.mprinter.textparser;

import uz.smd.mprinter.EscPosPrinterCommands;
import uz.smd.mprinter.exceptions.EscPosConnectionException;
import uz.smd.mprinter.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}
