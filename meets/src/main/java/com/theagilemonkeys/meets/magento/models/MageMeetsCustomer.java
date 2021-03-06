package com.theagilemonkeys.meets.magento.models;

import com.google.api.client.util.Key;
import com.theagilemonkeys.meets.ApiMethod;
import com.theagilemonkeys.meets.ApiMethodModelHelper;
import com.theagilemonkeys.meets.Meets;
import com.theagilemonkeys.meets.magento.MageApiMethodCollectionResponseClasses;
import com.theagilemonkeys.meets.magento.methods.CustomerAddressCreate;
import com.theagilemonkeys.meets.magento.methods.CustomerAddressDelete;
import com.theagilemonkeys.meets.magento.methods.CustomerAddressList;
import com.theagilemonkeys.meets.magento.methods.CustomerAddressUpdate;
import com.theagilemonkeys.meets.magento.methods.CustomerCustomerCreate;
import com.theagilemonkeys.meets.magento.methods.CustomerCustomerInfo;
import com.theagilemonkeys.meets.magento.methods.CustomerCustomerList;
import com.theagilemonkeys.meets.magento.methods.CustomerCustomerUpdate;
import com.theagilemonkeys.meets.magento.models.base.MageMeetsModel;
import com.theagilemonkeys.meets.models.MeetsAddress;
import com.theagilemonkeys.meets.models.MeetsCustomer;
import com.theagilemonkeys.meets.utils.StringUtils;
import com.theagilemonkeys.meets.utils.soap.SoapSerializableList;
import com.theagilemonkeys.meets.utils.soap.SoapSerializableMap;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Android Meets SDK
 * Original work Copyright (c) 2014 [TheAgileMonkeys]
 *
 * @author Álvaro López Espinosa
 */
public class MageMeetsCustomer extends MageMeetsModel<MeetsCustomer> implements MeetsCustomer {
    public final static String MODE_GUEST = "guest";
    public final static String MODE_CUSTOMER = "customer";

    @Key private String mode = MODE_CUSTOMER;
    @Key private int customer_id;
    @Key private String email;
    @Key private String firstname;
    @Key private String lastname;
    @Key private String taxvat;
    @Key private int store_id;
    @Key private int website_id;
    @Key private String password;
    @Key private String password_hash;
    @Key private List<MeetsAddress> addresses;

    {
        store_id = Meets.storeId;
        website_id = Meets.websiteId;
    }

    //This is specific of magento
    public MeetsCustomer setMode(String mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public MeetsCustomer fetch() {
        ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
            @Override
            public Map<String, Object> buildParams() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customerId", getId());
                return params;
            }
        };

        pushMethod(new CustomerCustomerInfo(), params)
                .done(updateFromResult)
                .always(triggerListeners);
        return this;
    }

    @Override
    public MeetsCustomer setId(int id) {
        customer_id = id;
        return this;
    }

    @Override
    public int getId() {
        return customer_id;
    }

    @Override
    public String getFirstName() {
        return firstname;
    }

    @Override
    public String getLastName() {
        return lastname;
    }

    @Override
    public String getFullName() {
        return StringUtils.safeValueOf(firstname) + " " + StringUtils.safeValueOf(lastname);
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getVatNumber() {
        return taxvat;
    }

    @Override
    public String getPasswordHash() {
        return password_hash;
    }

    @Override
    public MeetsAddress getDefaultBillingAddress() {
        for (MeetsAddress address : getAddresses()){
            if ( address.isDefaultBilling() ) return address;
        }
        return null;
    }

    @Override
    public MeetsAddress getDefaultShippingAddress() {
        for (MeetsAddress address : getAddresses()){
            if ( address.isDefaultShipping() ) return address;
        }
        return null;
    }

    @Override
    public List<MeetsAddress> getAddresses() {
        if (addresses == null)
            addresses = new ArrayList<MeetsAddress>();
        return addresses;
    }

    private MeetsCustomer create() {
        ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
            @Override
            public Map<String, Object> buildParams() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customerData", MageMeetsCustomer.this);
                return params;
            }
        };

        forceNextCacheToBe(false);
        pushMethod(new CustomerCustomerCreate(), params)
                .done(updateFromResult)
                .always(triggerListeners);
        return this;
    }

    @Override
    public MeetsCustomer save() {
        if (isNew()){
            return create();
        }
        ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
            @Override
            public Map<String, Object> buildParams() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customerId", getId());
                params.put("customerData", MageMeetsCustomer.this);
                return params;
            }
        };

        forceNextCacheToBe(false);
        pushMethod(new CustomerCustomerUpdate(), params).always(triggerListeners);
        return this;
    }

    @Override
    public MeetsCustomer fetchByEmail(final String email) {
        return internalFetchByEmail(email, null);
    }

    @Override
    public MeetsCustomer fetchByEmailAndCheckPassword(String email, String password) {
        return internalFetchByEmail(email, password);
    }

    private MeetsCustomer internalFetchByEmail(final String email, @Nullable final String password) {
        ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
            @Override
            public Map<String, Object> buildParams() {
                List<Map<String, Object>> complexFilter = new SoapSerializableList<Map<String, Object>>();
                    //Email filter key
                    Map<String, Object> filter = new SoapSerializableMap<String, Object>();
                        //Email filter value
                        Map<String, Object> filterValue = new SoapSerializableMap<String, Object>();
                        filterValue.put("key", "eq");
                        filterValue.put("value", email);
                    filter.put("key", "email");
                    filter.put("value", filterValue);
                complexFilter.add(filter);

                Map<String, Object> filters = new SoapSerializableMap<String, Object>();
                filters.put("complex_filter", complexFilter);

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("filters", filters);
                return params;
            }
        };

        forceNextCacheToBe(false);
        pushMethod(new CustomerCustomerList(), params);
        pushPipe(new DonePipe() {
            @Override
            public Deferred pipeDone(Object o) {
                MageApiMethodCollectionResponseClasses.Customers customers = (MageApiMethodCollectionResponseClasses.Customers) o;
                Exception e;
                if (customers.size() == 0) {
                    e = new Exception("Customer not found");
                }
                else{
                    MeetsCustomer customer = customers.get(0);
                    if (password == null || internalCheckPassword(password, customer.getPasswordHash())) {
                        updateFromResult(customer);
                        triggerListeners();
                        return new DeferredObject().resolve(MageMeetsCustomer.this);
                    }
                    e = new Exception("Incorrect password");
                }

                triggerListeners(e);
                return new DeferredObject().reject(e);
            }
        }, null);
        return this;
    }

    @Override
    public boolean checkPassword(String password) {
        return checkPassword(password);
    }

    private boolean internalCheckPassword(String password, String passwordHash){
        String[] pass_salt = passwordHash.split(":");
        String pass = pass_salt[0];
        String salt = pass_salt[1];
        String typedPassword = StringUtils.MD5Hash(salt + password);
        return pass.equals(typedPassword);
    }

    @Override
    public MeetsCustomer fetchAddresses() {
        ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
            @Override
            public Map<String, Object> buildParams() {
                Map<String,Object> params = new HashMap<String, Object>();
                params.put("customerId", getId());
                return params;
            }
        };

        pushMethod(new CustomerAddressList(), params)
                .done(new DoneCallback() {
                    @Override
                    public void onDone(Object o) {
                        addresses = (List) o;
                    }
                })
                .always(triggerListeners);
        return this;
    }

    @Override
    public MeetsCustomer saveAddress(final MeetsAddress meetsAddress) {
        ApiMethodModelHelper.DelayedParams params;
        ApiMethod method;
        if ( meetsAddress.isNew() ) {
            // We have to create the address
            params = new ApiMethodModelHelper.DelayedParams() {
                @Override
                public Map<String, Object> buildParams() {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("customerId", getId());
                    params.put("addressData", meetsAddress);
                    return params;
                }
            };
            method = new CustomerAddressCreate();
        }
        else {
            params = new ApiMethodModelHelper.DelayedParams() {
                @Override
                public Map<String, Object> buildParams() {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("addressId", meetsAddress.getId());
                    params.put("addressData", meetsAddress);
                    return params;
                }
            };
            method = new CustomerAddressUpdate();
        }

        forceNextCacheToBe(false);
        pushMethod(method, params)
                .done(new DoneCallback() {
                    @Override
                    public void onDone(Object o) {
                        if (o instanceof MeetsAddress)
                            meetsAddress.setId(((MeetsAddress)o).getId());
                        refreshAddressesAfterSave(meetsAddress);
                    }
                })
                .always(triggerListeners);
        return this;
    }

    @Override
    public MeetsCustomer removeAddress(final int addressId) {
        int indexToRemove = -1;
        for(int i = 0; i < getAddresses().size(); ++i) {
            if (getAddresses().get(i).getId() == addressId) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0) {
            final MeetsAddress addressToRemove = getAddresses().remove(indexToRemove);
            final int finalIndexToRemove = indexToRemove;

            ApiMethodModelHelper.DelayedParams params = new ApiMethodModelHelper.DelayedParams() {
                @Override
                public Map<String, Object> buildParams() {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("addressId", addressId);
                    return params;
                }
            };

            forceNextCacheToBe(false);
            pushMethod(new CustomerAddressDelete(), params)
                    .fail(new FailCallback() {
                        @Override
                        public void onFail(Object result) {
                            getAddresses().add(finalIndexToRemove, addressToRemove);
                        }
                    })
                    .always(triggerListeners);
        }
        return this;
    }

    private void refreshAddressesAfterSave(MeetsAddress savedAddress) {
        boolean addressIsNew = true;
        for (MeetsAddress address : addresses) {
            if (address.getId() == savedAddress.getId()) {
                address.shallowCopyFrom(savedAddress);
                addressIsNew = false;
            }
            else {
                if (savedAddress.isDefaultBilling() && address.isDefaultBilling())
                    address.setDefaultBilling(false);
                if (savedAddress.isDefaultShipping() && address.isDefaultShipping())
                    address.setDefaultShipping(false);
            }
        }
        if (addressIsNew)
            addresses.add(savedAddress);
    }

    @Override
    public MeetsCustomer setFirstName(String firstName) {
        this.firstname = firstName;
        return this;
    }

    @Override
    public MeetsCustomer setLastName(String lastname) {
        this.lastname = lastname;
        return this;
    }

    @Override
    public MeetsCustomer setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public MeetsCustomer setVatNumber(String taxvat) {
        this.taxvat = taxvat;
        return this;
    }

    @Override
    public MeetsCustomer setPassword(String password) {
        this.password = password;
        return this;
    }
}
