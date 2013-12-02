package io.stiefel.gvagrant

import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.Test

/**
 * @author Jason Stiefel <jason@stiefel.io>
 */
class VagrantTest {

    private AntBuilder ant = new AntBuilder()
    private Vagrant v;

    @AfterMethod
    void after() {
        if (v) {
            v.halt()
            v.destroy()
        }
    }

    @Test(expectedExceptions = FileNotFoundException)
    void fileNotFound() {
        v = new Vagrant(new File('src/test/resources/'))
    }

    @Test
    void lifecycle() {
        v = new Vagrant(stageVagrantFile('src/test/resources/default'))
        Assert.assertEquals(v.status(), Vagrant.Status.NOT_CREATED)
        v.up()
        Assert.assertEquals(v.status(), Vagrant.Status.RUNNING)
        v.halt()
        Assert.assertEquals(v.status(), Vagrant.Status.POWEROFF)
        v.destroy()
        Assert.assertEquals(v.status(), Vagrant.Status.NOT_CREATED)
    }

    private File stageVagrantFile(String path) {
        File source = new File(path)
        File stage = new File("./target/vagrants/${UUID.randomUUID().toString().toLowerCase()}")
        ant.copy(todir: stage) {
            fileset(dir: source)
        }
        new File(stage, 'Vagrantfile')
    }

}
