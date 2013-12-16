package com.antoinecampbell.gcmdemo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Example dialog that can be used to retrieve an Account from the user's device
 * 
 * @author Antoine
 * 
 */
public class AccountDialogFragment extends DialogFragment
{
    private Account[] mAccounts;
    private AccountSelectionListener mListener;

    public interface AccountSelectionListener
    {
	public void onAccountSelected(Account account);

	public void onDialogCanceled();
    }

    static AccountDialogFragment newInstance(AccountSelectionListener listener)
    {
	AccountDialogFragment dialogFragment = new AccountDialogFragment();
	dialogFragment.setAccountSelectedListener(listener);
	Bundle bundle = new Bundle();
	dialogFragment.setArguments(bundle);
	return dialogFragment;
    }

    /**
     * Set the {@code AccountSelectionListener}
     * 
     * @param listener
     */
    public void setAccountSelectedListener(AccountSelectionListener listener)
    {
	mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
	mAccounts = AccountManager.get(getActivity()).getAccounts();
	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("Select an account");
	builder.setCancelable(false);
	int size = mAccounts.length;
	String[] names = new String[size];
	for (int i = 0; i < size; i++)
	{
	    names[i] = mAccounts[i].name;
	}
	builder.setItems(names, new DialogInterface.OnClickListener()
	{
	    public void onClick(DialogInterface dialog, int which)
	    {
		// When the user selects an Account inform the listener
		if (mListener != null)
		{
		    mListener.onAccountSelected(mAccounts[which]);
		}
	    }
	});
	return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
	super.onCancel(dialog);
	if (mListener != null)
	{
	    mListener.onDialogCanceled();
	}
    }
}
