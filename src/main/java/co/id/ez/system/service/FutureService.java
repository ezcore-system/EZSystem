/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.id.ez.system.service;

/**
 *
 * @author Lutfi
 */
public abstract class FutureService extends Thread{
    
    public abstract void execute();

    @Override
    public void run() {
        execute();
    }
    
}
