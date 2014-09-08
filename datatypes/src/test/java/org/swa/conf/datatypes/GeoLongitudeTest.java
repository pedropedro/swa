package org.swa.conf.datatypes;

import java.math.BigDecimal;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.swa.conf.datatypes.validators.Range;

/**
 * Are there <code>decimal</code>s in the longitude range -180.000000 to +180.000000 (latitude is a subrange) making
 * problems in the {@link Range.RangeValidator} ?
 * <p/>
 * The test uses all available CPUs / cores !!!
 */
public class GeoLongitudeTest {

	public static void main(final String[] args) {
		new GeoLongitudeTest().decimalTest();
	}

	@Test
	@Ignore
	// runs too long on ancient quadcores i7 vPro ...
	public void decimalTest() {
		final long start = System.currentTimeMillis();
		final int count = new ForkJoinPool().invoke(new ForkComparator(0, 1, 0, 180, 0, 999999));
		System.out.println("Crunched decimals: " + count + " in " + ((System.currentTimeMillis() - start) / 1000) +
				" [s]");
	}

	static class ForkComparator extends RecursiveTask<Integer> {

		private static final long serialVersionUID = 1L;

		private final int sign_from;
		private final int sign_to;
		private final int decimal_from;
		private final int decimal_to;
		private final int fraction_from;
		private final int fraction_to;

		@SuppressWarnings("unused")
		private void log(final String vorspann) {
			System.out.println(vorspann + sign_from + "-" + sign_to + " " + String.format("%03d", decimal_from) + "-"
					+ String.format("%03d", decimal_to) + " " + fraction_from + "-" + fraction_to);
		}

		/**
		 * Compares all decimal numbers from range
		 * <p/>
		 * &lt <code>sign_from</code> <code>decimal_from</code>.<code>fraction_from</code> ; <code>sign_to</code>
		 * <code>decimal_to</code>.<code>fraction_to</code> &gt as {@link BigDecimal} and {@link Double}.
		 * <p/>
		 * Example for an interval: &lt -1.000000 ; -1.999999 &gt where sign_from == sign_to == 0, decimal_from ==
		 * decimal_to == 1 and fraction_from == 0, fraction_to == 999999
		 *
		 * @param sign_from
		 * 		signum - 0 == -, 1 == + (current range 'from' value)
		 * @param sign_to
		 * 		signum - 0 == -, 1 == + (current range 'to' value)
		 * @param decimal_from
		 * 		decimal part of the generated number (current range 'from' value)
		 * @param decimal_to
		 * 		decimal part of the generated number (current range 'to' value)
		 * @param fraction_from
		 * 		fractional part of the generated number (current range 'from' value)
		 * @param fraction_to
		 * 		fractional part of the generated number (current range 'to' value)
		 */
		ForkComparator(final int sign_from, final int sign_to, final int decimal_from, final int decimal_to,
					   final int fraction_from, final int fraction_to) {

			// log("New fork ");
			this.sign_from = sign_from;
			this.sign_to = sign_to;
			this.decimal_from = decimal_from;
			this.decimal_to = decimal_to;
			this.fraction_from = fraction_from;
			this.fraction_to = fraction_to;
		}

		private int computeLocal() {

			// log("Starting ");
			int counter = 0;

			for (int sign = sign_from; sign <= sign_to; sign++)
				for (int decimal = decimal_from; decimal <= decimal_to; decimal++)
					for (int fraction = fraction_from; fraction <= fraction_to; fraction++) {
						final String s = (sign == 0 ? "-" : "") + decimal + "." + String.format("%06d", fraction);
						counter++;
						final BigDecimal bd = new BigDecimal(s).stripTrailingZeros();
						final Double d = Double.valueOf(s);
						if (bd.compareTo(BigDecimal.valueOf(d)) != 0)
							Assert.fail(s);
					}

			return counter;
		}

		@Override
		protected Integer compute() {

			if (sign_from != sign_to) {

				final int splitPoint = (sign_to - sign_from) >>> 1;

				final ForkComparator f1 = new ForkComparator(sign_from, sign_from + splitPoint, decimal_from,
						decimal_to,
						fraction_from, fraction_to);
				f1.fork();

				final ForkComparator f2 = new ForkComparator(sign_from + 1 + splitPoint, sign_to, decimal_from,
						decimal_to,
						fraction_from, fraction_to);

				return f2.compute() + f1.join();

			} else if (decimal_from != decimal_to) {

				final int splitPoint = (decimal_to - decimal_from) >>> 1;

				final ForkComparator f1 = new ForkComparator(sign_from, sign_to, decimal_from,
						decimal_from + splitPoint,
						fraction_from, fraction_to);
				f1.fork();

				final ForkComparator f2 = new ForkComparator(sign_from, sign_to, decimal_from + 1 + splitPoint,
						decimal_to,
						fraction_from, fraction_to);

				return f2.compute() + f1.join();

			} else
				return computeLocal();
		}
	}
}