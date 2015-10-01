# FAQ for developers

## How should I install a Connector on the emulator/device?

When it comes to deploying the connectors to the emulator,
I just run the adb install -r <path/to/connector.apk> from shell.
I don't know, how to install it from eclipse directly, because there is no activity (in fact there is the shared info screen) to run.
If someone knows a better way, please contact me.

## How do I get the running ConnectorSpec?

Just use:

    ConnectorSpec c = this.getSpec(context);

Eg. to set balance you could use this code:

    String balance = getBalanceFromWebService(); // TODO implement me
    ConnectorSpec c = this.getSpec(context);
    c.setBalance(balance);
    Log.d(TAG, "balance: " + c.getBalance());

## In updateSpec() I get a ConnectorSpec object and I assumed that this is where I can update the balance since there is an setBalance method. As I do not have a ConnectorSpec object in doUpdate, I have to store the reference when updateSpec() initSpec() gets called and reuse it in doUpdate(). That might be the problem, but I haven't found another way so far. Is that the right method?

Wrong.
You must not do any network IO in `updateSpec()` or `initSpec()`.
Just set the basic capabilities, features and settings in `updateSpec()` and `initSpec()`.

Get the `ConnectorSpec` reference with `this.getSpec(context)` as mentioned above.

## More details

See [DeveloperHelp](https://github.com/felixb/websms-api/blob/master/doc/DeveloperHelp.md) for more details.
