/*
 * Copyright 2011-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hivewallet.androidclient.wallet.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import javax.annotation.Nonnull;

import android.view.View;
import android.widget.TextView;

import com.google.bitcoin.core.NetworkParameters;

import com.hivewallet.androidclient.wallet.Constants;

/**
 * @author Andreas Schildbach
 */
public class GenericUtils
{
	public static final BigInteger ONE_BTC = new BigInteger("100000000", 10);
	public static final BigInteger ONE_MBTC = new BigInteger("100000", 10);
	public static final BigInteger ONE_UBTC = new BigInteger("100", 10);

	private static final int ONE_BTC_INT = ONE_BTC.intValue();
	private static final int ONE_MBTC_INT = ONE_MBTC.intValue();
	private static final int ONE_UBTC_INT = ONE_UBTC.intValue();

	public static String formatValue(@Nonnull final BigInteger value, final int precision, final int shift)
	{
		return formatValue(value, "", "-", precision, shift);
	}

	public static String formatValue(@Nonnull final BigInteger value, @Nonnull final String plusSign, @Nonnull final String minusSign,
			final int precision, final int shift)
	{
		long longValue = value.longValue();

		final String sign = longValue < 0 ? minusSign : plusSign;

		if (shift == 0)
		{
			if (precision == 2)
				longValue = longValue - longValue % 1000000 + longValue % 1000000 / 500000 * 1000000;
			else if (precision == 4)
				longValue = longValue - longValue % 10000 + longValue % 10000 / 5000 * 10000;
			else if (precision == 6)
				longValue = longValue - longValue % 100 + longValue % 100 / 50 * 100;
			else if (precision == 8)
				;
			else
				throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);

			final long absValue = Math.abs(longValue);
			final long coins = absValue / ONE_BTC_INT;
			final int satoshis = (int) (absValue % ONE_BTC_INT);

			if (satoshis % 1000000 == 0)
				return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000000);
			else if (satoshis % 10000 == 0)
				return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10000);
			else if (satoshis % 100 == 0)
				return String.format(Locale.US, "%s%d.%06d", sign, coins, satoshis / 100);
			else
				return String.format(Locale.US, "%s%d.%08d", sign, coins, satoshis);
		}
		else if (shift == 3)
		{
			if (precision == 2)
				longValue = longValue - longValue % 1000 + longValue % 1000 / 500 * 1000;
			else if (precision == 4)
				longValue = longValue - longValue % 10 + longValue % 10 / 5 * 10;
			else if (precision == 5)
				;
			else
				throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);

			final long absValue = Math.abs(longValue);
			final long coins = absValue / ONE_MBTC_INT;
			final int satoshis = (int) (absValue % ONE_MBTC_INT);

			if (satoshis % 1000 == 0)
				return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000);
			else if (satoshis % 10 == 0)
				return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10);
			else
				return String.format(Locale.US, "%s%d.%05d", sign, coins, satoshis);
		}
		else if (shift == 6)
		{
			if (precision == 0)
				longValue = longValue - longValue % 100 + longValue % 100 / 50 * 100;
			else if (precision == 2)
				;
			else
				throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);

			final long absValue = Math.abs(longValue);
			final long coins = absValue / ONE_UBTC_INT;
			final int satoshis = (int) (absValue % ONE_UBTC_INT);

			if (satoshis % 100 == 0)
				return String.format(Locale.US, "%s%d", sign, coins);
			else
				return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis);
		}
		else
		{
			throw new IllegalArgumentException("cannot handle shift: " + shift);
		}
	}
	
	public static BigInteger parseValue(String value, int shift)
	{
		try {
			double dAmount = Double.parseDouble(value);
			int factor = ONE_BTC_INT / (int)Math.pow(10, shift);
			long amount = Math.round(dAmount * factor);
			return BigInteger.valueOf(amount);
		} catch (NumberFormatException e) {
			return BigInteger.ZERO;
		}		
	}

	public static String formatDebugValue(@Nonnull final BigInteger value)
	{
		return formatValue(value, Constants.BTC_MAX_PRECISION, 0);
	}

	public static BigInteger toNanoCoins(final String value, final int shift) throws ArithmeticException
	{
		final BigInteger nanoCoins = new BigDecimal(value).movePointRight(8 - shift).toBigIntegerExact();

		if (nanoCoins.signum() < 0)
			throw new ArithmeticException("negative amount: " + value);
		if (nanoCoins.compareTo(NetworkParameters.MAX_MONEY) > 0)
			throw new ArithmeticException("amount too large: " + value);

		return nanoCoins;
	}

	public static boolean startsWithIgnoreCase(final String string, final String prefix)
	{
		return string.regionMatches(true, 0, prefix, 0, prefix.length());
	}

	public static void setNextFocusForwardId(final View view, final int nextFocusForwardId)
	{
		try
		{
			final Method setNextFocusForwardId = TextView.class.getMethod("setNextFocusForwardId", Integer.TYPE);
			setNextFocusForwardId.invoke(view, nextFocusForwardId);
		}
		catch (final NoSuchMethodException x)
		{
			// expected on API levels below 11
		}
		catch (final Exception x)
		{
			throw new RuntimeException(x);
		}
	}
	
	public static String shortenString(@Nonnull final String string)
	{
		final int length = string.length();
		
		if (length <= 11)
		{
			return string;
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			
			sb.append(string.substring(0, 4));
			sb.append("...");
			sb.append(string.substring(length - 4, length));
			return sb.toString();
		}
	}
}