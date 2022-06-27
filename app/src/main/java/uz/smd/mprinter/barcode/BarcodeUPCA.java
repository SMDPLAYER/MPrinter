package uz.smd.mprinter.barcode;

import uz.smd.mprinter.EscPosPrinterCommands;
import uz.smd.mprinter.EscPosPrinterSize;
import uz.smd.mprinter.exceptions.EscPosBarcodeException;

public class BarcodeUPCA extends BarcodeNumber {

    public BarcodeUPCA(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_UPCA, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 12;
    }
}
