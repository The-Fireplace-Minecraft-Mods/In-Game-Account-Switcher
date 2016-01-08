package the_fireplace.ias.legacysupport;

import net.minecraft.util.StatCollector;

public class OldJava implements ILegacyCompat {
	@Override
	public int[] getDate() {
		int[] ret = new int[3];
		ret[0]=00;
		ret[1]=00;
		ret[2]=0000;
		return ret;
	}

	@Override
	public String getFormattedDate() {
		return StatCollector.translateToLocal("ias.updatejava");
	}

}
