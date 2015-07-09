/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zxt.dlna.dms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import com.zxt.dlna.activity.ContentActivity;
import com.zxt.dlna.application.ConfigData;
import com.zxt.dlna.dmp.ContentItem;

import android.app.Activity;
import android.os.Handler;

/**
 * Updates a tree model after querying a backend <em>ContentDirectory</em>
 * service.
 * 
 * @author Christian Bauer
 */
public class ContentBrowseActionCallback extends Browse {

	private static Logger log = Logger
			.getLogger(ContentBrowseActionCallback.class.getName());

	private Service service;

	private Container container;

	private ArrayList<ContentItem> list;

	private Activity activity;

	private Handler handler;

	public ContentBrowseActionCallback(Activity activity, Service service,
			Container container, ArrayList<ContentItem> list, Handler handler) {
		super(service, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
				null, new SortCriterion(true, "dc:title"));
		this.activity = activity;
		this.service = service;
		this.container = container;
		this.list = list;
		this.handler = handler;
	}

	public void received(final ActionInvocation actionInvocation,
			final DIDLContent didl) {
		log.fine("Received browse action DIDL descriptor, creating tree nodes");
		activity.runOnUiThread(new Runnable() {
			public void run() {
				try {
					list.clear();
					// Containers first
					for (Container childContainer : didl.getContainers()) {
						log.fine("add child container "
								+ childContainer.getTitle());
						list.add(new ContentItem(childContainer, service));
					}
					// Now items
					for (Item childItem : didl.getItems()) {
						log.fine("add child item" + childItem.getTitle());
						list.add(new ContentItem(childItem, service));
					}
				} catch (Exception ex) {
					log.fine("Creating DIDL tree nodes failed: " + ex);
					actionInvocation.setFailure(new ActionException(
							ErrorCode.ACTION_FAILED,
							"Can't create list childs: " + ex, ex));
					failure(actionInvocation, null);
					handler.sendEmptyMessage(ContentActivity.CONTENT_GET_FAIL);
				}
				ConfigData.listPhotos.clear();
				Iterator iterator = didl.getItems().iterator();
				while (iterator.hasNext()) {
					Item item = (Item) iterator.next();
					ContentItem contentItem = new ContentItem(item,
							ContentBrowseActionCallback.this.service);
					if ((contentItem.getItem().getTitle().toString() != null)
							&& (contentItem.getItem().getResources() != null)) {
						List list = contentItem.getItem().getResources();
						if ((list.size() != 0)
								&& (((Res) list.get(0)).getProtocolInfo() != null)
								&& (((Res) list.get(0)).getProtocolInfo()
										.getContentFormat() != null)) {
							if (((Res) list.get(0))
									.getProtocolInfo()
									.getContentFormat()
									.substring(
											0,
											((Res) list.get(0))
													.getProtocolInfo()
													.getContentFormat()
													.indexOf("/"))
									.equals("image")) {
								ConfigData.listPhotos
										.add(new ContentItem(
												item,
												ContentBrowseActionCallback.this.service));
							} else if (((Res) list.get(0))
									.getProtocolInfo()
									.getContentFormat()
									.substring(
											0,
											((Res) list.get(0))
													.getProtocolInfo()
													.getContentFormat()
													.indexOf("/"))
									.equals("audio")) {
								ConfigData.listAudios
										.add(new ContentItem(
												item,
												ContentBrowseActionCallback.this.service));
							} else {
								ConfigData.listVideos
										.add(new ContentItem(
												item,
												ContentBrowseActionCallback.this.service));
							}
						}
					}
				}
				handler.sendEmptyMessage(ContentActivity.CONTENT_GET_SUC);
			}
		});
	}

	public void updateStatus(final Status status) {
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation,
			final String defaultMsg) {
	}
}
